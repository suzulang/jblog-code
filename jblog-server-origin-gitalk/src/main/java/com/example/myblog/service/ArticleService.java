package com.example.myblog.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.example.myblog.common.CustomException;
import com.example.myblog.common.ResultAjax;
import com.example.myblog.dao.ArticleMapper;
import com.example.myblog.model.ArticleInfo;
import com.example.myblog.model.UserInfo;
import com.example.myblog.model.vo.UserInfoVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleService {
    @Autowired
    private UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    private ArticleMapper articleMapper;

    public List<ArticleInfo> getListByUid(int uid) {
        return articleMapper.getListByUid(uid);
    }

    public int addArticle(ArticleInfo articleInfo, Integer userId) {
        validateArticleInfo(articleInfo);
        initializeArticleInfo(articleInfo, userId);
        return articleMapper.add(articleInfo);
    }

    public int del(Integer aid, int uid) {
        // 校验文章ID和用户ID是否有效
        validateArticleOwnership(aid,uid);
        return articleMapper.del(aid, uid);
    }

    public Map<String, Object> getArticlesByPage(int pageNum, int pageSize) {
        // 使用 PageHelper 开始分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询文章列表
        List<ArticleInfo> articles = articleMapper.selectAllArticles();
        // 使用 PageInfo 包装查询结果
        PageInfo<ArticleInfo> pageInfo = new PageInfo<>(articles);
        // 封装响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("articles", articles); // 当前页的数据
        response.put("totalPages", pageInfo.getPages()); // 总页数
        response.put("totalElements", pageInfo.getTotal()); // 总记录数
        response.put("currentPage", pageInfo.getPageNum()); // 当前页码
        response.put("pageSize", pageInfo.getPageSize()); // 每页大小
        return response;
    }

    public int getCount(){
        return articleMapper.getCount();
    }

    public ArticleInfo getDetailById(int aid) {
        return articleMapper.getDetailById(aid);
    }

    public int getArtCountByUid(int uid) {
        return articleMapper.getArtCountByUid(uid);
    }

    public int incrementRCount(int aid) {
        return articleMapper.incrementRCount(aid);
    }

    public int updateArticle(ArticleInfo articleInfo) throws CustomException {
        UserInfo userInfo = (UserInfo) StpUtil.getSession().get("userInfo");
        validateArticleInfo(articleInfo);
        articleInfo.setUpdateTime(LocalDateTime.now());
        // 实现对文章归属人的验证等其他业务逻辑
        validateArticleOwnership(articleInfo.getId(), userInfo.getId());
        return articleMapper.update(articleInfo);
    }

    public ArticleInfo getArticleByIdAndUid(int aid, int uid) {
        return articleMapper.getArticleByIdAndUid(aid, uid);
    }

    /**
     * 安排文章在指定时间发布
     * @param articleInfo 文章信息
     * @param publishTimeStr 发布时间字符串（例如：'2023-09-01T15:00'）
     */
    public void scheduleArticlePublication(ArticleInfo articleInfo, String publishTimeStr) {
        // 将字符串时间转换为 Cron 表达式
        LocalDateTime publishTime = LocalDateTime.parse(publishTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String cronExpression = convertToCronExpression(publishTime);
        UserInfo userinfo = (UserInfo)StpUtil.getSession().get("userInfo");
        // 使用 HuTool 的 CronUtil 安排定时任务
        CronUtil.schedule(cronExpression, new Task() {
            @Override
            public void execute() {
                try {
                    logger.info("开始执行定时发布文章任务");
                    addArticle(articleInfo,userinfo.getId());
                    logger.info("定时发布文章任务成功完成");
                } catch (Exception e) {
                    logger.error("定时发布文章任务失败", e);
                }
            }
        });
    }

    private String convertToCronExpression(LocalDateTime publishTime) {
        // 将 LocalDateTime 转换为 Cron 表达式
        return String.format("%d %d %d %d %d ? %d",
                publishTime.getSecond(), publishTime.getMinute(), publishTime.getHour(),
                publishTime.getDayOfMonth(), publishTime.getMonthValue(), publishTime.getYear());
    }

    private void validateArticleInfo(ArticleInfo articleInfo) {
        if (articleInfo == null || !StringUtils.hasLength(articleInfo.getTitle()) ||
                !StringUtils.hasLength(articleInfo.getContent())) {
            throw new IllegalArgumentException("非法参数！");
        }
    }

    private void initializeArticleInfo(ArticleInfo articleInfo, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        articleInfo.setCreateTime(now);
        articleInfo.setUpdateTime(now);
        articleInfo.setReadCount(1);
        articleInfo.setUid(userId);
    }

    private void validateArticleOwnership(int articleId, Integer userId) throws CustomException {
        ArticleInfo existingArticle = articleMapper.getDetailById(articleId);
        if (existingArticle == null) {
            throw new CustomException(-3, "文章不存在！");
        }
        if (!(existingArticle.getUid()==userId)) {
            throw new CustomException(-4, "无权限修改他人文章！");
        }
    }

    public Map<String, Object> getArticleDetail(Integer aid) throws CustomException {
        if (aid == null || aid <= 0) {
            throw new CustomException(-1, "非法参数！");
        }

        ArticleInfo articleInfo = articleMapper.getDetailById(aid);
        if (articleInfo == null) {
            throw new CustomException(-3, "文章不存在！");
        }

        UserInfoVO userInfoVO = userService.getUserById(articleInfo.getUid());
        int artCount = getArtCountByUid(articleInfo.getUid());

        userInfoVO.setArtCount(artCount);
        HashMap<String, Object> result = new HashMap<>();
        result.put("user", userInfoVO);
        result.put("art", articleInfo);

        return result;
    }

}
