package com.example.myblog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 存储头像 URL 到 Redis
    public void saveAvatarUrl(String userId, String avatarUrl) {
        stringRedisTemplate.opsForValue().set("userAvatar:" + userId, avatarUrl);
    }

    // 从 Redis 获取头像 URL
    public String getAvatarUrl(String userId) {
        return stringRedisTemplate.opsForValue().get("userAvatar:" + userId);
    }
}


