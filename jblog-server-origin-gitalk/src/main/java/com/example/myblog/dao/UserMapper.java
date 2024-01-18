package com.example.myblog.dao;

import com.example.myblog.model.UserInfo;
import com.example.myblog.model.vo.UserInfoVO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("insert into userinfo(username, password, create_time, update_time,photo_link) values(#{username}, #{password}, #{createTime}, #{updateTime},#{photoLink})")
    int reg(UserInfo userInfo);

    @Select("select * from userinfo where username=#{username}")
    UserInfo getUserByName(@Param("username")String username);

    @Select("select * from userinfo where id=#{uid}")
    UserInfoVO getUserById(@Param("uid") int uid);
    @Update("update userinfo set photo_link = #{new_photoLink} where id = #{id}")
    int updatePhotoLink(@Param("id") int id, @Param("new_photoLink")String new_photoLink);
    @Select("select * from userinfo")
    List<UserInfo> getAllUsers();
}
