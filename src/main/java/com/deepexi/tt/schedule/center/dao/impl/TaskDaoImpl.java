package com.deepexi.tt.schedule.center.dao.impl;

import com.deepexi.tt.schedule.center.dao.ITaskDao;
import com.deepexi.tt.schedule.center.domain.bo.Task;
import com.deepexi.tt.schedule.center.domain.dataobject.TaskDo;
import com.deepexi.tt.schedule.center.mapper.TaskMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 白猛
 */
@Service
public class TaskDaoImpl implements ITaskDao {

    @Autowired
    TaskMapper taskMapper;

    @Override
    public Task save(Task task) {
        if (!ObjectUtils.isEmpty(task)) {
            TaskDo taskDo = new TaskDo();
            BeanUtils.copyProperties(task, taskDo);
            taskMapper.save(taskDo);
            BeanUtils.copyProperties(taskDo, task);
        }
        return task;
    }

    @Override
    public Task findById(Integer id) {
        TaskDo taskDo = taskMapper.findFirstById(id);
        Task task = new Task();
        if (!ObjectUtils.isEmpty(taskDo)) {
            BeanUtils.copyProperties(taskDo, task);
        }
        return task;
    }

    @Override
    public List<Task> findTaskQueue(Date date, Integer status) {
        List<TaskDo> taskDos = taskMapper.findAllByExecuteTimeBeforeAndStatus(date, status);
        List<Task> tasks = new ArrayList<>();
        taskDos.parallelStream().forEach(taskDo -> {
            Task task = new Task();
            BeanUtils.copyProperties(taskDo, task);
            tasks.add(task);
        });
        return tasks;
    }
}
