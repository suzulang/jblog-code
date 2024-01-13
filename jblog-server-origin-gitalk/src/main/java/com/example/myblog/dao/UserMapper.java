package com.example.myblog.dao;

import com.example.myblog.model.UserInfo;
import com.example.myblog.model.vo.UserInfoVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
public interface UserMapper {
    @Insert("insert into userinfo(username, password, create_time, update_time,photo_link) values(#{username}, #{password}, #{createTime}, #{updateTime},#{photoLink})")
    int reg(UserInfo userInfo);

    @Select("select * from userinfo where username=#{username}")
    UserInfo getUserByName(@Param("username")String username);

    @Select("select * from userinfo where id=#{uid}")
    UserInfoVO getUserById(@Param("uid") int uid);
}
