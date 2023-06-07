package com.xzh.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@MapperScan("com.xzh.usercenter.mapper")
@EnableRedisHttpSession
public class CustomerCenterApplication {

    public static void main(String[] args) {

        SpringApplication.run(CustomerCenterApplication.class, args);
    }

}
