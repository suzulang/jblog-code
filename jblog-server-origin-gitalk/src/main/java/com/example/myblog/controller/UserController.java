package com.example.myblog.controller;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import com.example.myblog.common.CustomException;
import com.example.myblog.common.PasswordUtils;
import com.example.myblog.common.ResultAjax;
import com.example.myblog.model.ArticleInfo;
import com.example.myblog.model.UserInfo;
import com.example.myblog.model.vo.UserInfoVO;
import com.example.myblog.service.ArticleService;
import com.example.myblog.service.RedisService;
import com.example.myblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;
    /**
     * 注册功能
     *
     * @param userInfo 用户信息
     * @param avatar   base64的字符串
     * @return 注册结果
     */
    @RequestMapping("/reg")
    public ResultAjax reg(UserInfo userInfo, String avatar) {

        // 请求 service 进行添加操作
        int result = userService.reg(userInfo, avatar);

        // 将执行的结果返回给前端
        return ResultAjax.succ(result);
    }

    /**
     * 登录接口
     *
     * @param userInfoVO
     * @return
     */

    @RequestMapping("/login")
    public ResultAjax login(UserInfoVO userInfoVO) {
        try {
            UserInfo userInfo = userService.loginUser(userInfoVO);

            // sa-token 登陆认证
            StpUtil.login(userInfo.getId());
            StpUtil.getSession().set("userInfo", userInfo);

            // 保存用户头像URL到Redis
            redisService.saveAvatarUrl(String.valueOf(userInfo.getId()), userInfo.getPhotoLink());

            return ResultAjax.succ(1);
        } catch (CustomException e) {
            return ResultAjax.fail(e.getStatusCode(), e.getMessage());
        }
    }

    /**
     * 注销
     */
    @RequestMapping("/logout")
    public ResultAjax logout() {
        // 调用sa-token的注销方法
        StpUtil.logout();
        // 返回成功
        return ResultAjax.succ(1);
    }

    @RequestMapping("/captcha")
    public void generateCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(300, 100, 4, 4);
        session.setAttribute("captcha", captcha.getCode());
        response.setContentType("image/png");
        captcha.write(response.getOutputStream());
    }

    /**
     * 这个接口用来检查的用户名是否已存在
     */

    @GetMapping("/checkUsername")
    public ResultAjax checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return ResultAjax.succ(exists);
    }

    @RequestMapping("/getPhotoLink")
    public ResultAjax getPhotoLink() {
        UserInfo userInfo = (UserInfo)StpUtil.getSession().get("userInfo");
        if (userInfo != null) {
            return ResultAjax.succ(redisService.getAvatarUrl(String.valueOf(userInfo.getId())));
        } else {
            return ResultAjax.fail(-1, "非法请求");
        }
    }
    @RequestMapping("/getPhotoLinkByAid")
    public ResultAjax getPhotoLink(Integer aid) {
        ArticleInfo articleInfo = articleService.getDetailById(aid);
        if (articleInfo != null) {
            return ResultAjax.succ(redisService.getAvatarUrl(String.valueOf(articleInfo.getUid())));
        } else {
            return ResultAjax.fail(-1, "非法请求");
        }
    }

    @GetMapping("/checkLogin")
    public ResultAjax checkLogin(){
        if (StpUtil.isLogin()) {
            return ResultAjax.succ(200,"已登录",true);
        } else {
            return ResultAjax.succ(-1,"未登录",false);
        }
    }
}
