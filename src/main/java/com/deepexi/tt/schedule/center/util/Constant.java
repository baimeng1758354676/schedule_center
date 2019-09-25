package com.deepexi.tt.schedule.center.util;

public class Constant {
    /**
     * 用来查询未来？分钟内即将执行的任务
     */
    public static final Integer TASK_TIME_LIMITED = 120;

    /**
     * 对应的毫秒数
     */
    public static final long TASK_TIME_LIMITED_IN_MILLIS = TASK_TIME_LIMITED * 60 * 1000;

    public static final String METHOD_GET = "GET";

    public static final String METHOD_POST = "POST";

    /**
     * 最大重试次数
     */
    public static final int MAX_ATTEMPTS = 4;

    /**
     * 重试延迟毫秒数
     */
    public static final long RETRY_DELAY = 1000L;

    /**
     * 重试延迟增加倍数
     */
    public static final double RETRY_DELAY_MULTIPLIER = 1.2;

    /**
     *任务队列消费者线程睡眠时间毫秒数
     */
    public static final long TASK_QUEUE_CONSUMER_THREAD_SLEEP_TIME_IN_MILLIS = 1000L;

    /**
     * 非法参数
     */
    public static final String ILLEGAL_PARAMETER = "非法参数！";

    /**
     * 核心线程数
     */
    public static final int CORE_POOL_SIZE = 5;
    /**
     * 最大线程数
     */
    public static final int MAX_POOL_SIZE = 5;
    /**
     * 活跃时间
     */
    public static final int KEEP_ALIVE_TIME = 60;





}
