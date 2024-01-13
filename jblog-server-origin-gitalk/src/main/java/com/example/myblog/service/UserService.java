package com.example.myblog.service;

import com.example.myblog.common.CustomException;
import com.example.myblog.common.PasswordUtils;
import com.example.myblog.common.QiniuUtils;
import com.example.myblog.dao.UserMapper;
import com.example.myblog.model.UserInfo;
import com.example.myblog.model.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QiniuUtils qiniuUtils;

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
                // 生成文件名（例如使用用户名或者唯一ID）
                String fileName = "avatar_" + System.currentTimeMillis(); // 使用时间戳保证唯一性
                // 上传头像到七牛云
                qiniuUtils.upload2Qiniu(avatar, fileName);
                // 更新用户信息中的头像URL
                userInfo.setPhotoLink(qiniuUtils.getDownloadUrl(fileName));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("头像上传失败");
            }
        }

        // 向数据库添加用户信息
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
}
