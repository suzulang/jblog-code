package com.example.myblog.common;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PasswordUtils {
    /**
     * 加盐加密
     *
     * @param password
     * @return
     */
    public static String encrypt(String password) {
        // 1.盐值
        String salt = UUID.randomUUID().toString().replace("-", "");
        // 2.将盐值+密码进行 md5 得到最终密码
        String finalPassword = DigestUtils.md5DigestAsHex((salt + password).getBytes(StandardCharsets.UTF_8));
        // 3.将盐值和最终密码进行返回
        return salt + "$" + finalPassword;
    }

    /**
     * 加盐验证
     *
     * @param password   待验证密码
     * @param dbPassword 数据库中的密码=盐值+分隔符+最终密码
     * @return
     */
    public static boolean decrypt(String password, String dbPassword) {
        if (!StringUtils.hasLength(password) || !StringUtils.hasLength(dbPassword) ||
                dbPassword.length() != 65) {
            return false;
        }
        // 1.得到盐值
        String[] dbPasswordArray = dbPassword.split("\\$");
        if (dbPasswordArray.length != 2) {
            return false;
        }
        // 盐值
        String salt = dbPasswordArray[0];
        // 最终正确密码
        String dbFinalPassword = dbPasswordArray[1];
        // 2.加密待验证的密码
        String finalPassword = DigestUtils.md5DigestAsHex((salt + password)
                .getBytes(StandardCharsets.UTF_8));
        // 3.对比
        if(finalPassword.equals(dbFinalPassword)){
            return true;
        }
        return false;
    }
}
