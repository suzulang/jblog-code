package com.example.myblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@MapperScan("com.example.myblog.dao") // 此包下的所有类都是 mapper 类
@EnableRedisRepositories
public class MyblogApplication  {

    public static void main(String[] args) {
        SpringApplication.run(MyblogApplication.class, args);
    }

}
