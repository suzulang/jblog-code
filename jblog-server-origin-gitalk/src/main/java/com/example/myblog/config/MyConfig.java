package com.example.myblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 系统配置文件
 */
@Configuration
@Controller
public class MyConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginIntercept())
                .addPathPatterns("/**")
                .excludePathPatterns("/login.html")
                .excludePathPatterns("/editor.md/*")
                .excludePathPatterns("/img/*")
                .excludePathPatterns("/js/*")
                .excludePathPatterns("/css/*")
                .excludePathPatterns("/avatar_*")
                .excludePathPatterns("/gen")
                .excludePathPatterns("/check")
                .excludePathPatterns("/blog_list.html")
                .excludePathPatterns("/user/*")
                .excludePathPatterns("/art/*")
                .excludePathPatterns("/reg.html")
                .excludePathPatterns("/blog_content.html");
    }

    @RequestMapping("/")
    public String index() {
        return "redirect:/login.html";
    }
}
