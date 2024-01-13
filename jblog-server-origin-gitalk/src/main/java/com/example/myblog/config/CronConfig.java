package com.example.myblog.config;

import cn.hutool.cron.CronUtil;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class CronConfig {

    @PostConstruct
    public void init() {
        // 启动 HuTool 的 Cron 调度器
        CronUtil.start();
    }
}
