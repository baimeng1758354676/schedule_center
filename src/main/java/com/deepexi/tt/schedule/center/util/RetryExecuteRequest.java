package com.deepexi.tt.schedule.center.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.deepexi.tt.schedule.center.enums.HttpExceptionMessageEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


/**
 * @Author: 白猛
 * @Date: 2019/9/25 17:47
 */
@Service
public class RetryExecuteRequest {

    private static Logger logger = LoggerFactory.getLogger(RetryExecuteRequest.class);


    @Retryable(value = Exception.class, maxAttempts = Constant.MAX_ATTEMPTS, backoff = @Backoff(delay = Constant.RETRY_DELAY, multiplier = Constant.RETRY_DELAY_MULTIPLIER))
    public boolean executeRequest(HttpRequest request) throws Exception {
        logger.info("执行请求（重试）……");
        if (ObjectUtils.isEmpty(request)) {
            throw new Exception(HttpExceptionMessageEnums.REQUEST_IS_NULL);
        }
        HttpResponse response = request.execute();
        if (ObjectUtils.isEmpty(response)) {
            throw new Exception(HttpExceptionMessageEnums.RESPONSE_IS_NULL);
        }
        if (response.getStatus() == HttpStatus.HTTP_OK) {
            logger.info("ok!");
            return true;
        }
        return false;
    }

    @Recover
    public boolean maxAttemptFailed(Exception e) {
        logger.info("重试耗尽……");
        return false;
    }

}
