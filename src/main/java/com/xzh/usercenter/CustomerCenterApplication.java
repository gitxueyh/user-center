package com.xzh.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xzh.usercenter.mapper")
public class CustomerCenterApplication {

    public static void main(String[] args) {

        SpringApplication.run(CustomerCenterApplication.class, args);
    }

}
