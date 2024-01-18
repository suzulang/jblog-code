package com.example.myblog;

import com.example.myblog.common.QiniuUtils;
import com.example.myblog.model.ArticleInfo;
import com.example.myblog.model.UserInfo;
import com.example.myblog.service.ArticleService;
import com.example.myblog.service.RedisService;
import com.example.myblog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MyblogApplicationTests {

    @Autowired
    RedisService redisService;
    @Autowired
    UserService userService;

    @Test
    public void test(){
        List<UserInfo> allUsers = userService.getAllUsers();
        System.out.println(allUsers);
    }
    @Test void testDownLoadUrl(){
        String url = QiniuUtils.getDownloadUrl("avatar_1702396465964");
        System.out.println(url);
    }

    @Test
    public void testCron(){
        userService.updatePhotoLink();
    }
}
