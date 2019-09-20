package com.deepexi.tt.schedule.center.service;

import com.deepexi.tt.schedule.center.domain.bo.Task;

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
     * 执行任务
     */
    void executeTask();
}
