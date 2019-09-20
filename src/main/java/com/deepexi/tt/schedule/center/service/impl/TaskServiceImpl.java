package com.deepexi.tt.schedule.center.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpStatus;
import com.deepexi.tt.schedule.center.dao.ITaskDao;
import com.deepexi.tt.schedule.center.domain.bo.Task;
import com.deepexi.tt.schedule.center.enums.TaskStatusEnums;
import com.deepexi.tt.schedule.center.service.ITaskService;
import com.deepexi.tt.schedule.center.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements ITaskService {

    private static Queue<Task> taskQueue = new PriorityBlockingQueue<>();
    @Autowired
    ITaskDao taskDao;
    Function<Task, HttpRequest> cvt = t -> {
        if (t == null) {
            return null;
        }
        String mt = Optional.ofNullable(t.getMethod()).orElse("").toUpperCase();
        HttpRequest request = null;
        switch (mt) {
            case Constant.METHOD_GET:
                request = HttpRequest.get(t.getUrl());
                break;
            case Constant.METHOD_POST:
                request = HttpRequest.post(t.getUrl()).body(t.getData());
                break;
            default:
                request = HttpRequest.get(t.getUrl());
        }
        return request;
    };
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public Task save(Task task) {
        taskQueue.add(task);
        return taskDao.save(task);
    }

    @Override
    @Scheduled(cron = "0/5 * * * * ?")
    public void findTaskQueue() {
        //查询近期要处理的任务集合
        List<Task> tasks = taskDao.findTaskQueue
                (new Date(System.currentTimeMillis() + Constant.TASK_TIME_LIMITED_IN_MILLIS),
                        TaskStatusEnums.TASK_STATUS_NOT_PUT);
        //放入任务队列
        tasks.parallelStream().forEach(task -> {
            if (!taskQueue.contains(task)) {
                if (taskQueue.add(task)) {
                    //成功放入内存，修改状态为   已放入
                    task.setStatus(TaskStatusEnums.TASK_STATUS_PUT);
                    taskDao.save(task);
                }
            }
        });
    }

    @Override
    @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask() {
        List<Task> collect = taskQueue.parallelStream()
                .filter(task -> new Date().after(task.getExecuteTime()))
                .collect(Collectors.toList());
        lock.lock();
        taskQueue.removeAll(collect);
        lock.unlock();
        collect.parallelStream().forEach(task -> {
            if (new Date().after(task.getExecuteTime())) {
                //执行请求
                Integer rs = executeRequest(cvt.apply(task))
                        ? TaskStatusEnums.TASK_STATUS_EXECUTED_SUCCESS
                        : TaskStatusEnums.TASK_STATUS_EXECUTED_FAILED;
                task.setStatus(rs);
                //从队列中移除
                taskQueue.remove(task);
                //更新数据库的任务状态（成功/失败）
                taskDao.save(task);
            }
        });
    }

    private boolean executeRequest(HttpRequest request) {
        int count = 0;
        while (true) {
            if (request.execute().getStatus() == HttpStatus.HTTP_OK) {
                System.out.println(111);
                return true;
            }
            count++;
            if (count >= Constant.REQUEST_TIME) {
                break;
            }
        }
        return false;
    }
}
