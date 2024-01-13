package com.example.myblog.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserInfo implements Serializable {
    private int id;
    private String username;
    private String password;
    private String photoLink;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int state;

}
