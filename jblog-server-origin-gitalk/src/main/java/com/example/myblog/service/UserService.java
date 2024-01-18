package com.example.myblog.service;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.example.myblog.common.CustomException;
import com.example.myblog.common.PasswordUtils;
import com.example.myblog.common.QiniuUtils;
import com.example.myblog.dao.UserMapper;
import com.example.myblog.model.UserInfo;
import com.example.myblog.model.vo.UserInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QiniuUtils qiniuUtils;

    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    public int reg(UserInfo userInfo, String avatar){
        // 参数校验（可根据需求调整）
        if (userInfo == null || userInfo.getUsername() == null || userInfo.getPassword() == null) {
            throw new IllegalArgumentException("非法参数");
        }

        // 密码加盐
        userInfo.setPassword(PasswordUtils.encrypt(userInfo.getPassword()));

        // 处理头像上传
        if (avatar != null) {
            try {
                // 生成文件名，用用户id作为唯一键
                String fileName = "avatar_" + userInfo.getId(); //
                // 上传头像到七牛云
                qiniuUtils.upload2Qiniu(avatar, fileName);
                // 获取可以访问的外链
                String photo_link = qiniuUtils.getDownloadUrl(fileName);
                // 给userinfo表中的对应的photo_link字段设置值
                userInfo.setPhotoLink(qiniuUtils.getDownloadUrl(fileName));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("头像上传失败");
            }
        }

        // 向数据库添加封装好的用户信息
        return userMapper.reg(userInfo);
    }

    public UserInfo loginUser(UserInfoVO userInfoVO) throws CustomException {
        // 参数校验
        validateUserInfoVO(userInfoVO);

        // 根据用户名查询用户
        UserInfo userInfo = userMapper.getUserByName(userInfoVO.getUsername());
        if (userInfo == null || userInfo.getId() == 0) {
            throw new CustomException(-2, "用户或密码错误！");
        }

        // 密码验证
        if (!PasswordUtils.decrypt(userInfoVO.getPassword(), userInfo.getPassword())) {
            throw new CustomException(-2, "用户或密码错误！");
        }

        return userInfo;
    }

    private void validateUserInfoVO(UserInfoVO userInfoVO) throws CustomException {
        if (userInfoVO == null || !StringUtils.hasLength(userInfoVO.getUsername()) ||
                !StringUtils.hasLength(userInfoVO.getPassword())) {
            throw new CustomException(-1, "非法参数！");
        }
    }
    public UserInfo getUserByName(String username){
        return userMapper.getUserByName(username);
    }

    public UserInfoVO getUserById(int uid){
        return userMapper.getUserById(uid);
    }

    public boolean checkUsernameExists(String username) {
        UserInfo userInfo = userMapper.getUserByName(username);
        return userInfo != null;
    }

    public List<UserInfo> getAllUsers(){
        return userMapper.getAllUsers();
    }

    /**
     * 获取七牛云上的新 URL 并更新数据库
     */
    public  void updatePhotoLink() {
        List<UserInfo> allUsers = userMapper.getAllUsers();
        allUsers.forEach(userInfo -> {
            // 为每个用户获取新的照片链接
            String newPhotoLink = QiniuUtils.getDownloadUrl("avatar_" + userInfo.getId());
            // 更新数据库中该用户的 photoLink 字段
            userMapper.updatePhotoLink(userInfo.getId(), newPhotoLink);
        });
    }

    @PostConstruct
    public void init() {
        scheduleUpdatePhotoLinkTask();
    }

    public void scheduleUpdatePhotoLinkTask() {
        // 安排定时任务，每天午夜执行
        CronUtil.schedule("0 58 19 * * *", new Task() {
            @Override
            public void execute() {
                updatePhotoLink();
            }
        });
    }


}
