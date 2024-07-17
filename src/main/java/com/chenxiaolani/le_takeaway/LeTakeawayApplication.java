package com.chenxiaolani.le_takeaway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j // 这是lombok提供的日志打印方式
@SpringBootApplication // 这是springboot的启动类注解
@ServletComponentScan // 这个注解是为了扫描filter
@EnableTransactionManagement // 开启事务管理
@EnableCaching // 开启注解缓存
public class LeTakeawayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeTakeawayApplication.class, args);
        // 这是lombok提供的日志打印方式
        log.info("项目启动成功...");
    }

}
