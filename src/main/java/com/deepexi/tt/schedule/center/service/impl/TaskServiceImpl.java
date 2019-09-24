package com.deepexi.tt.schedule.center.service.impl;

import cn.hutool.http.HttpRequest;
import com.deepexi.tt.schedule.center.dao.ITaskDao;
import com.deepexi.tt.schedule.center.domain.bo.Task;
import com.deepexi.tt.schedule.center.enums.QueueCapacityEnums;
import com.deepexi.tt.schedule.center.enums.TaskStatusEnums;
import com.deepexi.tt.schedule.center.service.IExecuteRequestService;
import com.deepexi.tt.schedule.center.service.ITaskService;
import com.deepexi.tt.schedule.center.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Function;

/**
 * @author 白猛
 */
@Service
@Component
public class TaskServiceImpl implements ITaskService, CommandLineRunner {

    private static BlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>(QueueCapacityEnums.QUEUE_INITIAL_CAPACITY, Comparator.comparingLong(t -> t.getExecuteTime().getTime()));

//    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 6, 60, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());

    @Autowired
    ITaskDao taskDao;

    @Autowired
    IExecuteRequestService executeRequestService;

    Function<Task, HttpRequest> cvt = t -> {
        if (t == null) {
            return null;
        }
        String mt = Optional.ofNullable(t.getMethod()).orElse("").toUpperCase();
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
    public Task save(Task task) {
        task.setStatus(TaskStatusEnums.TASK_STATUS_NOT_EXECUTED);
        task.setId(null);
        //判断并加入任务队列
        taskDao.save(task);
        judgeAndAddToTaskQueue(task);
        return task;
    }

    private void judgeAndAddToTaskQueue(Task task) {
        if (Optional.ofNullable(task).isPresent()
                && Optional.ofNullable(task.getExecuteTime()).isPresent()
                && task.getExecuteTime().before(new Date(System.currentTimeMillis() + Constant.TASK_TIME_LIMITED_IN_MILLIS))
                && !taskQueue.parallelStream().filter(t -> t.getId().equals(task.getId())).findAny().isPresent()) {
            taskQueue.add(task);
        }
    }

    @Override
    @Scheduled(cron = "0/9 * * * * ? ")
    public void findTaskQueue() {
        //查询近期未处理的任务集合
        System.out.println(new Date(System.currentTimeMillis() + Constant.TASK_TIME_LIMITED_IN_MILLIS));
        List<Task> tasks = taskDao.findTaskQueue
                (new Date(System.currentTimeMillis() + Constant.TASK_TIME_LIMITED_IN_MILLIS),
                        TaskStatusEnums.TASK_STATUS_NOT_EXECUTED);

        tasks.parallelStream().forEach(task -> {
            //如果不在队列中，则加入队列
            if (!taskQueue.parallelStream().filter(t -> t.getId().equals(task.getId())).findAny().isPresent()) {
                System.out.println("provider : " + task);
                taskQueue.add(task);
                System.out.println("队列长度 ：" + taskQueue.size());
            }
        });
    }


    /**
     * 消费任务队列
     */
    @Override
    public void consumeTaskQueue() {
        while (true) {
            try {
                Task task = taskQueue.take();
                System.out.println("consumer:" + task);
                if (task.getExecuteTime().before(new Date())) {
                    //满足执行条件，执行请求
                    Integer status = executeRequestService.executeRequest(cvt.apply(task))
                            ? TaskStatusEnums.TASK_STATUS_EXECUTED_SUCCESS
                            : TaskStatusEnums.TASK_STATUS_EXECUTED_FAILED;
                    task.setStatus(status);
                    taskDao.save(task);
                    System.out.println("consumer : success");
                } else {
                    taskQueue.add(task);
                }
                Thread.sleep(Constant.TASK_QUEUE_CONSUMER_THREAD_SLEEP_TIME_IN_MILLIS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Task getTask(Integer id) {
        return taskDao.findById(id);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("启动队列消费者……");
        consumeTaskQueue();
    }
}
