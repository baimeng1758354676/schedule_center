package com.deepexi.tt.schedule.center.dao;

import com.deepexi.tt.schedule.center.domain.bo.Task;

import java.util.Date;
import java.util.List;

/**
 * @author 白猛
 */
public interface ITaskDao {

    /**
     * 新增/更新任务
     *
     * @param task
     * @return
     */
    Task save(Task task);


    /**
     * 根据id查询任务
     *
     * @param id
     * @return
     */
    Task findById(Integer id);

    /**
     * 查询要存在内存中的任务集合
     *
     * @param date
     * @param status
     * @return
     */
    List<Task> findTaskQueue(Date date, Integer status);

}
