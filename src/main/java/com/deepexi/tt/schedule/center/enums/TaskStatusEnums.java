package com.deepexi.tt.schedule.center.enums;

/**
 * @Author
 */
public interface TaskStatusEnums {

    /**
     * 未放入内存状态码
     */
    Integer TASK_STATUS_NOT_PUT = 0;

    /**
     * 已放入内存
     */
    Integer TASK_STATUS_PUT = 1;

    /**
     * 执行失败
     */
    Integer TASK_STATUS_EXECUTED_FAILED = 2;

    /**
     * 执行成功
     */
    Integer TASK_STATUS_EXECUTED_SUCCESS = 3;
}
