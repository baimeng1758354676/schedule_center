package com.deepexi.tt.schedule.center.service;

import com.deepexi.tt.schedule.center.domain.bo.Task;

/**
 * @Author: 白猛
 * @Date: 2019/9/23 17:46
 */
public interface IExecuteTaskService {


    /**
     * 执行任务
     * @param take 要执行的任务
     */
    void executeTask(Task take);
}
