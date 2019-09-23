package com.deepexi.tt.schedule.center.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.deepexi.tt.schedule.center.enums.HttpExceptionMessageEnums;
import com.deepexi.tt.schedule.center.service.IExecuteRequestService;
import com.deepexi.tt.schedule.center.util.Constant;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @Author: 白猛
 * @Date: 2019/9/23 17:49
 */
@Service
public class ExecuteRequestServiceImpl implements IExecuteRequestService {

    @Override
    @Retryable(value = Exception.class, maxAttempts = Constant.MAX_ATTEMPTS, backoff = @Backoff(delay = 1000L, multiplier = 1.2))
    public boolean executeRequest(HttpRequest request) throws Exception {
        System.out.println("执行请求（重试）……");
        if (ObjectUtils.isEmpty(request)) {
            throw new Exception(HttpExceptionMessageEnums.REQUEST_IS_NULL);
        }
        HttpResponse response = request.execute();
        if (ObjectUtils.isEmpty(response)) {
            throw new Exception(HttpExceptionMessageEnums.RESPONSE_IS_NULL);
        }
        if (response.getStatus() == HttpStatus.HTTP_OK) {
            System.out.println("ok");
            return true;
        }
        return false;
    }


    @Override
    @Recover
    public boolean maxAttemptFailed(Exception e) {
        //邮件通知调用者……
        System.out.println("重试耗尽……");
        return false;
    }

}
