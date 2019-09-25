package com.deepexi.tt.schedule.center.service.impl;

import cn.hutool.core.date.DateUtil;
import com.deepexi.tt.schedule.center.dao.ITaskDao;
import com.deepexi.tt.schedule.center.domain.bo.Task;
import com.deepexi.tt.schedule.center.enums.QueueCapacityEnums;
import com.deepexi.tt.schedule.center.enums.TaskStatusEnums;
import com.deepexi.tt.schedule.center.service.IExecuteTaskService;
import com.deepexi.tt.schedule.center.service.ITaskManager;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author 白猛
 */
@Service
@Component
public class TaskManagerImpl implements ITaskManager {

    private static Logger logger = LoggerFactory.getLogger(TaskManagerImpl.class);

    private static BlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>(QueueCapacityEnums.QUEUE_INITIAL_CAPACITY, Comparator.comparingLong(t -> t.getExecuteTime().getTime()));


    @Autowired
    ITaskDao taskDao;

    @Autowired
    IExecuteTaskService executeTaskService;



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
            if (!ObjectUtils.isEmpty(task)) {
                logger.info("取得一个任务");
                if (DateUtil.compare(task.getExecuteTime(), new Date()) <= 0) {
                    executeTaskService.executeTask(taskQueue.take());
                }
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
