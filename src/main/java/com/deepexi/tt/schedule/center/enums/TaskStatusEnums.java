package com.deepexi.tt.schedule.center.enums;

/**
 * @Author 白猛
 */
public interface TaskStatusEnums {

    /**
     * 未执行状态码
     */
    Integer TASK_STATUS_NOT_EXECUTED = 0;


    /**
     * 执行失败
     */
    Integer TASK_STATUS_EXECUTED_FAILED = 1;

    /**
     * 执行成功
     */
    Integer TASK_STATUS_EXECUTED_SUCCESS = 2;
}
