package com.hpboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.hpboot.mapper")
@SpringBootApplication
public class HpbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(HpbootApplication.class, args);
    }

}
