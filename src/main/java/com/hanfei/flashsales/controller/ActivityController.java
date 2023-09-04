package com.hanfei.flashsales.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
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
     * Handle getting all activities requests, applying feat: Sentinel rate limiting and feat: page caching
     */
    @GetMapping(value = "/all", produces = "text/html;charset=utf-8")
    public String all(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        // Sentinel rate limiting with resource “activityAll”
        try (Entry entry = SphU.entry("activityAll")) {
            log.info("Request activity/all, userId: [{}]", user.getUserId());

            // If activityAll html is cached in Redis
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            String html = (String) valueOperations.get("activityAll");
            if (!StringUtils.isEmpty(html)) {
                log.info("===> Content found from Redis");
                return html;
            }

            // If activityAll html is not cached in Redis
            List<ListVO> activeActivityVOs = activityService.getActiveActivityVOs();
            model.addAttribute("user", user);
            model.addAttribute("activityVOs", activeActivityVOs);

            // Create a WebContext to facilitate rendering the 'activityAll' Thymeleaf template
            WebContext webContext = new WebContext(
                    request,
                    response,
                    request.getServletContext(),
                    request.getLocale(),
                    model.asMap()
            );

            // Process the 'activityAll' Thymeleaf template using the configured Thymeleaf view resolver
            html = thymeleafViewResolver.getTemplateEngine().process("activityAll", webContext);
            if (!StringUtils.isEmpty(html)) {
                valueOperations.set("activityAll", html, 60, TimeUnit.SECONDS);
            }
            log.info("===> Content not found from Redis");
            return html;

        } catch (BlockException e) {
            log.warn("Request blocked due to excessive clicking, userId: [{}]", user.getUserId());
            return "Clicking too fast, please try again later...";
        }
    }

    /**
     * Handle getting activity detail requests
     */
    @GetMapping("/detail/{activityId}")
    public Result detail(User user, @PathVariable Long activityId) {
        log.info("Request activity/detail, userId: [{}], activityId: [{}]", user.getUserId(), activityId);

        // Fetch activity and commodity detail from mysql
        Activity activity = activityService.getActivityById(activityId);
        Commodity commodity = commodityService.getCommodityById(activity.getCommodityId());

        // Initialize sale status and remaining seconds
        int saleStatus = 0; // 0: not started
        int remainSeconds;
        LocalDateTime startDateTime = activity.getStartTime();
        LocalDateTime endDateTime = activity.getEndTime();
        LocalDateTime nowDateTime = LocalDateTime.now();

        // Determine the sale status and remaining time
        if (nowDateTime.isBefore(startDateTime)) {
            Duration duration = Duration.between(nowDateTime, startDateTime);
            remainSeconds = (int) duration.getSeconds();
        } else if (nowDateTime.isAfter(endDateTime)) {
            saleStatus = 2; // 2: ended
            remainSeconds = -1;
        } else {
            saleStatus = 1; // 1: started
            remainSeconds = 0;
        }

        // Create a DetailVO object to encapsulate the data
        DetailVO detailVO = new DetailVO();
        detailVO.setActivity(activity);
        detailVO.setCommodity(commodity);
        detailVO.setUserId(user.getUserId());
        detailVO.setUsername(user.getUsername());
        detailVO.setRemainSeconds(remainSeconds);
        detailVO.setSaleStatus(saleStatus);

        return Result.success(detailVO);
    }
}
