package com.example.myblog.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.myblog.common.CustomException;
import com.example.myblog.common.ResultAjax;
import com.example.myblog.model.ArticleInfo;
import com.example.myblog.model.UserInfo;
import com.example.myblog.model.vo.ArticleInfoVO;
import com.example.myblog.service.ArticleService;
import com.example.myblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@RestController
@RequestMapping("/art")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    private static final int _DESC_LENGTH = 120; // 文章简介的长度
    @Autowired
    private UserService userService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 得到当前登录用户的文章列表
     *
     * @return
     */
    @RequestMapping("/mylist")
    public Object myList() {

        try {
            // 1.得到当前登录用户
            UserInfo userInfo = (UserInfo) StpUtil.getSession().get("userInfo");
            // 2.根据用户 id 查询此用户发表的所有文章
            List<ArticleInfo> list = articleService.getListByUid(userInfo.getId());
            // 处理 list -> 将文章正文变成简介
            if (list != null && list.size() > 0) {
                // 并行处理 list 集合
                list.stream().parallel().forEach((art) -> {
                    if (art.getContent().length() > _DESC_LENGTH) {
                        art.setContent(art.getContent().substring(0, _DESC_LENGTH));
                    }
                });
            }
            String photoUrl = userInfo.getPhotoLink();
            // 3.封装数据到Map中
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("articles", list);
            responseData.put("photoUrl", photoUrl);
            // 4.返回给前端
            return ResultAjax.succ(responseData);

        } catch (Exception e) {
            return new ModelAndView("redirect:/login.html");
        }
    }

    /**
     * 添加文章
     */
    @RequestMapping("/add")
    public ResultAjax add(ArticleInfo articleInfo) {
        try {
            // 从session中获取userInfo
            UserInfo userInfo = (UserInfo) StpUtil.getSession().get("userInfo");
            // 调用服务层添加文章
            int result = articleService.addArticle(articleInfo, userInfo.getId());
            return ResultAjax.succ(result);
        } catch (IllegalArgumentException e) {
            return ResultAjax.fail(-1, e.getMessage());
        }
    }

    /**
     * 删除文章
     */
    @RequestMapping("/del")
    public ResultAjax del(Integer aid) {
        UserInfo userInfo = (UserInfo) StpUtil.getSession().get("userInfo");
        int result = articleService.del(aid, userInfo.getId());
        return ResultAjax.succ(result);
    }

    /**
     * 查询文章详情页
     */
    @RequestMapping("/detail")
    public ResultAjax detail(Integer aid) {
        try {
            return ResultAjax.succ(articleService.getArticleDetail(aid));
        } catch (CustomException e) {
            return ResultAjax.fail(e.getStatusCode(), e.getMessage());
        }
    }

    @RequestMapping("/incrementRCount")
    public ResultAjax incrementRCount(Integer aid) {
        // 1.效验参数
        if (aid == null || aid <= 0) {
            return ResultAjax.fail(-1, "参数有误！");
        }
        // 2.更改数据库 update articleinfo set rcount=rcount+1 where aid=#{aid}
        int result = articleService.incrementRCount(aid);
        // 3.返回结果
        return ResultAjax.succ(result);
    }

    /**
     * 修改文章信息
     */
    @RequestMapping("/update")
    public ResultAjax update(ArticleInfo articleInfo) {
        try {
            UserInfo userinfo = (UserInfo)StpUtil.getSession().get("userInfo");
            articleInfo.setUid(userinfo.getId());
            int result = articleService.updateArticle(articleInfo);
            return ResultAjax.succ(result);
        } catch (CustomException e) {
            return ResultAjax.fail(e.getStatusCode(), e.getMessage());
        }
    }

    /**
     * 查询自己发表的文章详情
     */
    @RequestMapping("/update_init")
    public ResultAjax updateInit(Integer aid) {
        // 1.参数效验
        if (aid == null || aid <= 0) {
            return ResultAjax.fail(-1, "参数有误！");
        }
        // 2.得到当前登录用户 id
        UserInfo userInfo = (UserInfo) StpUtil.getSession().get("userInfo");
        if (userInfo == null) {
            return ResultAjax.fail(-2, "请先登录！");
        }
        // 3.查询文章并效验权限 where id=#{aid} and uid=#{uid}
        ArticleInfo articleInfo = articleService.getArticleByIdAndUid(
                aid, userInfo.getId()
        );
        // 4.将结果返回给前端
        return ResultAjax.succ(articleInfo);
    }

    /**
     * 查询分页功能
     */
    @RequestMapping("/getListByPage")
    public ResultAjax getListByPage(@RequestParam(defaultValue = "1") int pindex,
                                    @RequestParam(defaultValue = "2") int psize) {
        Map<String, Object> res = articleService.getArticlesByPage(pindex, psize);
        return ResultAjax.succ(res);
    }

    /**
     * 安排定时发布文章
     */
    @PostMapping("/schedulePost")
    public ResultAjax schedulePost(@RequestBody ArticleInfoVO articleInfoVO) {
        // 1.参数效验
        if (articleInfoVO == null || !StringUtils.hasLength(articleInfoVO.getTitle()) ||
                !StringUtils.hasLength(articleInfoVO.getContent()) || !StringUtils.hasLength(articleInfoVO.getPublishTime())) {
            return ResultAjax.fail(-1, "非法参数！");
        }

        // 2.调用 Service 层方法安排定时发布
        try {
            articleService.scheduleArticlePublication(articleInfoVO, articleInfoVO.getPublishTime());
            return ResultAjax.succ(1); // 假设成功返回 1
        } catch (Exception e) {
            return ResultAjax.fail(-1, "定时发布失败：" + e.getMessage());
        }
    }


}
