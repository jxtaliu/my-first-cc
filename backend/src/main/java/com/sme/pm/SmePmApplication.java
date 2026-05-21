package com.sme.pm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sme.pm.mapper")
public class SmePmApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmePmApplication.class, args);
    }
}
