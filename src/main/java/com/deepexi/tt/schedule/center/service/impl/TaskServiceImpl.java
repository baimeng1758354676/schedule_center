package com.deepexi.tt.schedule.center.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.deepexi.tt.schedule.center.dao.ITaskDao;
import com.deepexi.tt.schedule.center.domain.bo.Task;
import com.deepexi.tt.schedule.center.enums.QueueCapacityEnums;
import com.deepexi.tt.schedule.center.enums.TaskStatusEnums;
import com.deepexi.tt.schedule.center.service.IExecuteRequestService;
import com.deepexi.tt.schedule.center.service.ITaskService;
import com.deepexi.tt.schedule.center.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author 白猛
 */
@Service
@Component
public class TaskServiceImpl implements ITaskService {

    private static Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private static BlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>(QueueCapacityEnums.QUEUE_INITIAL_CAPACITY, Comparator.comparingLong(t -> t.getExecuteTime().getTime()));

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Constant.CORE_POOL_SIZE, Constant.MAX_POOL_SIZE, Constant.KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

    @Autowired
    ITaskDao taskDao;

    @Autowired
    IExecuteRequestService executeRequestService;

    Function<Task, HttpRequest> cvt = t -> {
        if (t == null) {
            return null;
        }
        String mt = Optional.ofNullable(t.getMethod()).orElse("").toUpperCase().trim();
        HttpRequest request = null;
        String url = t.getUrl();
        switch (mt) {
            case Constant.METHOD_GET:
                request = HttpRequest.get(url);
                break;
            case Constant.METHOD_POST:
                request = HttpRequest.post(url).body(t.getData());
                break;
            default:
                request = HttpRequest.get(url);
        }
        return request;
    };


    @Override
    public Object save(Task task) {
        task.setStatus(TaskStatusEnums.TASK_STATUS_NOT_EXECUTED);
        task.setId(null);
        //校验参数
        if (ObjectUtils.isEmpty(task.getExecuteTime())
                || ObjectUtils.isEmpty(task.getCreateTime())
                || ObjectUtils.isEmpty(task.getUrl())
                || ObjectUtils.isEmpty(task.getMethod())
                || (task.getMethod().trim().toUpperCase().equals(Constant.METHOD_POST) && ObjectUtils.isEmpty(task.getData()))) {
            return Constant.ILLEGAL_PARAMETER;
        }
        //判断并加入任务队列
        taskDao.save(task);
        judgeAndAddToTaskQueue(task);
        return task;
    }

    private void judgeAndAddToTaskQueue(Task task) {
        if (task.getExecuteTime().before(new Date(System.currentTimeMillis() + Constant.TASK_TIME_LIMITED_IN_MILLIS))
                && !taskQueue.parallelStream().filter(t -> t.getId().equals(task.getId())).findAny().isPresent()) {
            taskQueue.add(task);
        }
    }

    @Override
    @Scheduled(cron = "0 0 0/2 * * ? ")
    public void findTaskQueue() {
        //查询近期未处理的任务集合
        System.out.println(new Date(System.currentTimeMillis() + Constant.TASK_TIME_LIMITED_IN_MILLIS));
        List<Task> tasks = taskDao.findTaskQueue
                (new Date(System.currentTimeMillis() + Constant.TASK_TIME_LIMITED_IN_MILLIS),
                        TaskStatusEnums.TASK_STATUS_NOT_EXECUTED);

        taskQueue.clear();
        taskQueue.addAll(tasks);
    }


    /**
     * 消费任务队列
     */
    @Override
    public void consumeTaskQueue() {
        try {
            Task task = taskQueue.peek();
            if (task != null) {
                logger.info("取得一个任务");
                if (DateUtil.compare(task.getExecuteTime(), new Date()) <= 0) {
                    Task task2 = taskQueue.take();
                    //满足执行条件，执行请求
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.info("执行任务");
                            Integer status = null;
                            try {
                                status = executeRequestService.executeRequest(cvt.apply(task2))
                                        ? TaskStatusEnums.TASK_STATUS_EXECUTED_SUCCESS
                                        : TaskStatusEnums.TASK_STATUS_EXECUTED_FAILED;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            task2.setStatus(status);
                            taskDao.save(task2);
                            logger.info("consumer : success");
                        }
                    });
                }
                Thread.sleep(Constant.TASK_QUEUE_CONSUMER_THREAD_SLEEP_TIME_IN_MILLIS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Task getTask(Integer id) {
        return taskDao.findById(id);
    }

}
