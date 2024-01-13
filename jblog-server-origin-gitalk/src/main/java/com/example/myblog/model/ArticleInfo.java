package com.example.myblog.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
public class ArticleInfo implements Serializable {
    private int id;
    private String title;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone = "GMT+8")
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int uid;
    private int readCount;
    private int state;
}
