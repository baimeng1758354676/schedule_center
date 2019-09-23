package com.deepexi.tt.schedule.center.service;

import cn.hutool.http.HttpRequest;

/**
 * @Author: 白猛
 * @Date: 2019/9/23 17:46
 */
public interface IExecuteRequestService {
    /**
     * 执行请求
     *
     * @param request
     * @return
     */
    boolean executeRequest(HttpRequest request) throws Exception;


    /**
     * 最大次数重试失败之后的操作（failure alert）
     *
     * @param e
     * @return
     */
    boolean maxAttemptFailed(Exception e);
}
