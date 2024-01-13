package com.example.myblog.dao;

import com.example.myblog.model.ArticleInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {
    @Select("select * from articleinfo where uid=#{uid} order by id desc")
    List<ArticleInfo> getListByUid(@Param("uid") int uid);
    @Insert("insert into articleinfo(title, content, uid, create_time, update_time, read_count) values(#{title}, #{content}, #{uid}, #{createTime}, #{updateTime}, COALESCE(#{readCount}, 1))")
    int add(ArticleInfo articleInfo);
    @Delete("delete from articleinfo where id=#{aid} and uid=#{uid}")
    int del(@Param("aid") Integer aid, int uid);

    @Select("SELECT * FROM articleinfo")
    List<ArticleInfo> selectAllArticles();

    @Select("select count(*) from articleinfo")
    int getCount();
    @Select("select * from articleinfo where id=#{aid}")
    ArticleInfo getDetailById(@Param("aid") int aid);

    @Select("select count(*) from articleinfo where uid=#{uid}")
    int getArtCountByUid(@Param("uid") int uid);

    @Update("update articleinfo set read_count=read_count+1 where id=#{aid}")
    int incrementRCount(@Param("aid") int aid);

    @Update("update articleinfo set title=#{title}, content=#{content}, update_time=#{updateTime} where id=#{id} and uid=#{uid}")
    int update(ArticleInfo articleInfo);

    @Select("select * from articleinfo where id=#{aid} and uid=#{uid}")
    ArticleInfo getArticleByIdAndUid(@Param("aid") int aid,
                                     @Param("uid") int uid);


}
