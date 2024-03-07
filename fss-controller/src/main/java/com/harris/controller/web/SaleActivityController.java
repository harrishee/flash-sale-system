package com.harris.controller.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.saleactivity.SaleActivityAppService;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.converter.SaleActivityConverter;
import com.harris.controller.model.request.PublishActivityRequest;
import com.harris.controller.model.response.SaleActivityResponse;
import com.harris.domain.model.enums.SaleActivityStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sale-activities")
public class SaleActivityController {
    @Resource
    private SaleActivityAppService saleActivityAppService;
    
    // 根据活动ID获取活动详情
    @GetMapping("/{activityId}")
    public SingleResponse<SaleActivityResponse> getActivityById(@RequestAttribute Long userId,
                                                                @PathVariable Long activityId,
                                                                @RequestParam(required = false) Long version) {
        // 调用应用层的 获取活动 方法
        // 应用层 -> 本地缓存 -> 分布式缓存 -> 最新状态缓存 / 稍后再试（tryLater） / 不存在（notExist）
        // 高并发读活动请求被阻止在应用层，通过缓存实现数据获取，不会让请求进入领域层。
        // 只有在分布式 数据尚未被缓存 和 缓存失效 的情况下，才会进入领域层获取数据。
        AppSingleResult<SaleActivityDTO> activityResult = saleActivityAppService.getActivity(userId, activityId, version);
        if (!activityResult.isSuccess() || activityResult.getData() == null) {
            return ResponseConverter.toSingleResponse(activityResult);
        }
        
        SaleActivityDTO activityDTO = activityResult.getData();
        SaleActivityResponse activityResponse = SaleActivityConverter.toResponse(activityDTO);
        return SingleResponse.of(activityResponse);
    }
    
    // 获取活动列表
    @GetMapping
    public MultiResponse<SaleActivityResponse> listActivities(@RequestAttribute Long userId,
                                                              @RequestParam Integer pageSize,
                                                              @RequestParam Integer pageNumber,
                                                              @RequestParam(required = false) String keyword) {
        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        
        // 调用应用层的 获取活动列表 方法
        // 对第一页且无关键字的查询，走缓存；其他情况走数据库
        AppMultiResult<SaleActivityDTO> listResult = saleActivityAppService.listActivities(userId, activitiesQuery);
        if (!listResult.isSuccess() || listResult.getData() == null) {
            return ResponseConverter.toMultiResponse(listResult);
        }
        
        return MultiResponse.of(SaleActivityConverter.toResponseList(listResult.getData()), listResult.getTotal());
    }
    
    // 获取在线活动列表
    @GetMapping("/online")
    public MultiResponse<SaleActivityResponse> listOnlineActivities(@RequestAttribute Long userId,
                                                                    @RequestParam Integer pageSize,
                                                                    @RequestParam Integer pageNumber,
                                                                    @RequestParam(required = false) String keyword) {
        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(SaleActivityStatus.ONLINE.getCode());
        
        // 调用应用层的 获取活动列表 方法
        // 对第一页且无关键字的查询，走缓存；其他情况走数据库
        AppMultiResult<SaleActivityDTO> listResult = saleActivityAppService.listActivities(userId, activitiesQuery);
        if (!listResult.isSuccess() || listResult.getData() == null) {
            return ResponseConverter.toMultiResponse(listResult);
        }
        
        return MultiResponse.of(SaleActivityConverter.toResponseList(listResult.getData()), listResult.getTotal());
    }
    
    // 发布活动
    @PostMapping
    public Response publishActivity(@RequestAttribute Long userId,
                                    @RequestBody PublishActivityRequest publishActivityRequest) {
        // 调用应用层的 发布活动 方法
        // 应用层加分布式锁，用户防抖，key = ACTIVITY_CREATE_LOCK + userId
        PublishActivityCommand publishActivityCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult publishResult = saleActivityAppService.publishActivity(userId, publishActivityCommand);
        return ResponseConverter.toResponse(publishResult);
    }
    
    // 修改活动
    @PutMapping("/{activityId}")
    public Response modifyActivity(@RequestAttribute Long userId,
                                   @PathVariable Long activityId,
                                   @RequestBody PublishActivityRequest publishActivityRequest) {
        // 调用应用层的 修改活动 方法
        // 应用层加分布式锁，防止并发修改活动，key = ACTIVITY_MODIFICATION_LOCK + activityId
        PublishActivityCommand modifyActivityCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult modifyResult = saleActivityAppService.modifyActivity(userId, activityId, modifyActivityCommand);
        return ResponseConverter.toResponse(modifyResult);
    }
    
    // 上线活动
    @PutMapping("/{activityId}/online")
    public Response onlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        // 调用应用层的 上线活动 方法
        // 应用层加分布式锁，防止并发修改活动，key = ACTIVITY_MODIFICATION_LOCK + activityId
        AppResult onlineResult = saleActivityAppService.onlineActivity(userId, activityId);
        return ResponseConverter.toResponse(onlineResult);
    }
    
    // 下线活动
    @PutMapping("/{activityId}/offline")
    public Response offlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        // 调用应用层的 下线活动 方法
        // 应用层加分布式锁，防止并发修改活动，key = ACTIVITY_MODIFICATION_LOCK + activityId
        AppResult offlineResult = saleActivityAppService.offlineActivity(userId, activityId);
        return ResponseConverter.toResponse(offlineResult);
    }
}
