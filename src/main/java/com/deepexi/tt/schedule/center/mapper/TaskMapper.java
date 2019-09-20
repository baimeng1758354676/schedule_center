package com.deepexi.tt.schedule.center.mapper;

import com.deepexi.tt.schedule.center.domain.dataobject.TaskDo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface TaskMapper extends JpaRepository<TaskDo, Integer> {

    TaskDo findFirstById(Integer id);


    /**
     * 查询要存在内存中的任务
     *
     * @param date
     * @param status
     * @return
     */
    List<TaskDo> findAllByExecuteTimeBeforeAndStatus(Date date, Integer status);
}
