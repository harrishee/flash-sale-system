package com.hanfei.flashsales.controller;

import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.pojo.Commodity;
import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.ActivityService;
import com.hanfei.flashsales.service.CommodityService;
import com.hanfei.flashsales.vo.DetailVO;
import com.hanfei.flashsales.vo.ListVO;
import com.hanfei.flashsales.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@RestController
@RequestMapping("activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private CommodityService commodityService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 跳转商品列表页面 页面缓存
     */
    @GetMapping(value = "/all", produces = "text/html;charset=utf-8")
    public String list(Model model, User user, HttpServletRequest req, HttpServletResponse resp) {
        log.info("***Controller*** 商品列表页面被用户: {} 访问", user.getUserId());

        // 从缓存中获取页面，如果有，直接返回页面
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("activityAll");
        if (!StringUtils.isEmpty(html)) {
            log.info("=====> 加载到缓存的内容");
            return html;
        }

        // 未从缓存中查到，需要从数据库中联表查询一次
        List<ListVO> activityVOs = activityService.getActiveActivityVOs();

        model.addAttribute("user", user);
        model.addAttribute("activityVOs", activityVOs);

        // 如果缓存中没有，手动渲染，存入缓存
        WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("activityAll", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("activityAll", html, 60, TimeUnit.SECONDS);
        }
        log.info("=====> 没加载到缓存的内容");
        return html;
    }

    /**
     * 跳转商品详情页面
     */
    @GetMapping("/detail/{activityId}")
    public Result detail(User user, @PathVariable Long activityId) {
        log.info("***Controller*** 商品详情页面被用户: {} 访问", user.getUserId());

        // 数据库中查询两次
        Activity activity = activityService.getActivityById(activityId);
        Commodity commodity = commodityService.getCommodityById(activity.getCommodityId());

        // 秒杀状态，0 表示秒杀未开始
        int seckillStatus = 0;
        int remainSeconds;

        LocalDateTime startDateTime = activity.getStartTime();
        LocalDateTime endDateTime = activity.getEndTime();
        LocalDateTime nowDateTime = LocalDateTime.now();

        // 判断秒杀状态
        if (nowDateTime.isBefore(startDateTime)) {
            Duration duration = Duration.between(nowDateTime, startDateTime);
            remainSeconds = (int) duration.getSeconds();
        } else if (nowDateTime.isAfter(endDateTime)) {
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            seckillStatus = 1;
            remainSeconds = 0;
        }

        // 封装数据
        DetailVO detailVO = new DetailVO();
        detailVO.setActivity(activity);
        detailVO.setCommodity(commodity);
        detailVO.setUserId(user.getUserId());
        detailVO.setUsername(user.getUsername());
        detailVO.setRemainSeconds(remainSeconds);
        detailVO.setSecKillStatus(seckillStatus);

        return Result.success(detailVO);
    }
}
