package com.deepexi.tt.schedule.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 白猛
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry
@EnableAsync
public class ScheduleCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScheduleCenterApplication.class, args);
    }
}
