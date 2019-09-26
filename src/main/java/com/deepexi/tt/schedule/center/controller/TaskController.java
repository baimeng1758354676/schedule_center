package com.deepexi.tt.schedule.center.controller;

import com.deepexi.tt.schedule.center.domain.bo.Task;
import com.deepexi.tt.schedule.center.service.ITaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 白猛
 */
@RestController
@RequestMapping(path = "/task")
public class TaskController {

    @Autowired
    ITaskManager taskService;

    @PostMapping(path = "/")
    public Object newTask(@RequestBody Task task) {
        return taskService.save(task);
    }

    @GetMapping(path = "/{id}")
    public Task getTask(@PathVariable("id") Integer id) {
        return taskService.getTask(id);
    }

}
