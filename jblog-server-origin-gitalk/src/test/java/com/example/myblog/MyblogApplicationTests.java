package com.example.myblog;

import com.example.myblog.model.ArticleInfo;
import com.example.myblog.model.UserInfo;
import com.example.myblog.service.ArticleService;
import com.example.myblog.service.RedisService;
import com.example.myblog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyblogApplicationTests {

    @Autowired
    RedisService redisService;
}
