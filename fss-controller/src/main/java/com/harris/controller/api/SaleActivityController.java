package com.harris.controller.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.SaleActivityAppService;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.converter.SaleActivityConverter;
import com.harris.controller.model.request.PublishActivityRequest;
import com.harris.controller.model.response.SaleActivityResponse;
import com.harris.domain.model.enums.SaleActivityStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;

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
        AppSingleResult<SaleActivityDTO> activityResult = saleActivityAppService.getActivity(userId, activityId, version);
        SaleActivityDTO activityDTO = activityResult.getData();
        SaleActivityResponse activityResponse = SaleActivityConverter.toResponse(activityDTO);
        
        // 检查获取活动是否 失败 或者 为空
        return !activityResult.isSuccess() || activityDTO == null
                ? ResponseConverter.toSingleResponse(activityResult)
                : SingleResponse.of(activityResponse);
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
        
        // 调用应用层的 获取活动列表 方法（走的是缓存）
        AppMultiResult<SaleActivityDTO> listResult = saleActivityAppService.listActivities(userId, activitiesQuery);
        Collection<SaleActivityDTO> activityDTOs = listResult.getData();
        Collection<SaleActivityResponse> activitiesResponses = SaleActivityConverter.toResponseList(activityDTOs);
        
        // 检查获取活动列表是否 失败 或者 为空
        return !listResult.isSuccess() || activityDTOs == null
                ? ResponseConverter.toMultiResponse(listResult)
                : MultiResponse.of(activitiesResponses, listResult.getTotal());
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
        
        // 调用应用层的 获取活动列表 方法（走的是数据库）
        AppMultiResult<SaleActivityDTO> listResult = saleActivityAppService.listActivities(userId, activitiesQuery);
        Collection<SaleActivityDTO> activityDTOs = listResult.getData();
        Collection<SaleActivityResponse> activitiesResponses = SaleActivityConverter.toResponseList(activityDTOs);
        
        // 检查获取活动列表是否 失败 或者 为空
        return !listResult.isSuccess() || activityDTOs == null
                ? ResponseConverter.toMultiResponse(listResult)
                : MultiResponse.of(activitiesResponses, listResult.getTotal());
    }
    
    // 发布活动
    @PostMapping
    public Response publishActivity(@RequestAttribute Long userId,
                                    @RequestBody PublishActivityRequest publishActivityRequest) {
        // 调用应用层的 发布活动 方法
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
        PublishActivityCommand modifyActivityCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult modifyResult = saleActivityAppService.modifyActivity(userId, activityId, modifyActivityCommand);
        return ResponseConverter.toResponse(modifyResult);
    }
    
    // 上线活动
    @PutMapping("/{activityId}/online")
    public Response onlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        // 调用应用层的 上线活动 方法
        AppResult onlineResult = saleActivityAppService.onlineActivity(userId, activityId);
        return ResponseConverter.toResponse(onlineResult);
    }
    
    // 下线活动
    @PutMapping("/{activityId}/offline")
    public Response offlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        // 调用应用层的 下线活动 方法
        AppResult offlineResult = saleActivityAppService.offlineActivity(userId, activityId);
        return ResponseConverter.toResponse(offlineResult);
    }
}
