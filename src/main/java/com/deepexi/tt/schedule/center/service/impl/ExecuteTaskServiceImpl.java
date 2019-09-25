package com.deepexi.tt.schedule.center.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.deepexi.tt.schedule.center.dao.ITaskDao;
import com.deepexi.tt.schedule.center.domain.bo.Task;
import com.deepexi.tt.schedule.center.enums.TaskStatusEnums;
import com.deepexi.tt.schedule.center.service.IExecuteTaskService;
import com.deepexi.tt.schedule.center.util.Constant;
import com.deepexi.tt.schedule.center.util.RetryExecuteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 白猛
 * @Date: 2019/9/23 17:49
 */
@Service
public class ExecuteTaskServiceImpl implements IExecuteTaskService {


    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Constant.CORE_POOL_SIZE, Constant.MAX_POOL_SIZE, Constant.KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());


    private static Logger logger = LoggerFactory.getLogger(ExecuteTaskServiceImpl.class);


    @Autowired
    RetryExecuteRequest retryExecuteRequest;

    @Autowired
    ITaskDao taskDao;

    @Override
    public void executeTask(Task task) {
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpRequest request = getRequestFromTask(task);
                    boolean flag = true;
                    do {
                        flag = !retryExecuteRequest.executeRequest(request);
                        if (!DateUtil.isIn(new Date(), new Date(task.getExecuteTime().getTime() - Constant.TASK_EXECUTE_TIME_ERROR_RANGE), new Date(task.getExecuteTime().getTime() + Constant.TASK_EXECUTE_TIME_ERROR_RANGE))) {
                            taskFailed(task);
                            return;
                        }
                    } while (flag);
                    taskSucceed(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void taskSucceed(Task task) {
        task.setStatus(TaskStatusEnums.TASK_STATUS_EXECUTED_SUCCESS);
        taskDao.save(task);
        logger.info("任务成功！");
    }

    private void taskFailed(Task task) {
        task.setStatus(TaskStatusEnums.TASK_STATUS_EXECUTED_FAILED);
        taskDao.save(task);
        logger.info("任务失败！");
    }


    private HttpRequest getRequestFromTask(Task task) {
        if (task == null) {
            return null;
        }
        String mt = Optional.ofNullable(task.getMethod()).orElse("").toUpperCase().trim();
        HttpRequest request = null;
        String url = task.getUrl();
        switch (mt) {
            case Constant.METHOD_GET:
                request = HttpRequest.get(url);
                break;
            case Constant.METHOD_POST:
                request = HttpRequest.post(url).body(task.getData());
                break;
            default:
                request = HttpRequest.get(url);
        }
        return request;
    }


}
