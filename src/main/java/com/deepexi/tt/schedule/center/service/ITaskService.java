package com.deepexi.tt.schedule.center.service;

import com.deepexi.tt.schedule.center.domain.bo.Task;

/**
 * @author 白猛
 */
public interface ITaskService {
    /**
     * 新增任务
     *
     * @param task
     * @return
     */
    Task save(Task task);


    /**
     * 查询任务集合，定时执行
     *
     * @return
     */
    void findTaskQueue();


    /**
     * 消费任务队列
     */
    void consumeTaskQueue();

    /**
     * 查询单个任务
     *
     * @param id
     * @return
     */
    Task getTask(Integer id);
}
