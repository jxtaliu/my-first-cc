package com.sme.pm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.sme.pm.mapper")
@EnableAsync
@EnableScheduling
public class SmePmApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmePmApplication.class, args);
    }
}
