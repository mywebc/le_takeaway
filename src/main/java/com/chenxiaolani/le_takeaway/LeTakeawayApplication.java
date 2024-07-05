package com.chenxiaolani.le_takeaway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class LeTakeawayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeTakeawayApplication.class, args);
        // 这是lombok提供的日志打印方式
        log.info("项目启动成功...");
    }

}
