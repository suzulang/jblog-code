package com.example.myblog.model.vo;

import com.example.myblog.model.UserInfo;
import lombok.Data;

@Data
public class UserInfoVO extends UserInfo {
    private String checkCode;
    private int artCount;
}
