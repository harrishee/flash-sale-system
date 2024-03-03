package com.harris.controller.resource;

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
        // 从应用层获取活动详情
        AppSingleResult<SaleActivityDTO> activityResult = saleActivityAppService.getActivity(userId, activityId, version);
        
        // 将活动详情转换为响应对象
        SaleActivityDTO activityDTO = activityResult.getData();
        SaleActivityResponse activityResponse = SaleActivityConverter.toResponse(activityDTO);
        
        // 如果获取活动详情失败或者活动详情为空，则返回对应的错误响应；否则返回成功响应并携带活动详情
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
        // 构建查询对象
        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery().setKeyword(keyword).setPageSize(pageSize).setPageNumber(pageNumber);
        
        // 从应用层获取活动列表
        AppMultiResult<SaleActivityDTO> listResult = saleActivityAppService.listActivities(userId, activitiesQuery);
        
        // 将活动列表转换为响应对象
        Collection<SaleActivityDTO> activityDTOs = listResult.getData();
        Collection<SaleActivityResponse> activitiesResponses = SaleActivityConverter.toResponseList(activityDTOs);
        
        // 如果获取活动列表失败或者活动列表为空，则返回对应的错误响应；否则返回成功响应并携带活动列表
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
        // 构建查询对象
        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(SaleActivityStatus.ONLINE.getCode());
        
        // 从应用层获取活动列表
        AppMultiResult<SaleActivityDTO> listResult = saleActivityAppService.listActivities(userId, activitiesQuery);
        
        // 将活动列表转换为响应对象
        Collection<SaleActivityDTO> activityDTOs = listResult.getData();
        Collection<SaleActivityResponse> activitiesResponses = SaleActivityConverter.toResponseList(activityDTOs);
        
        // 如果获取活动列表失败或者活动列表为空，则返回对应的错误响应；否则返回成功响应并携带活动列表
        return !listResult.isSuccess() || activityDTOs == null
                ? ResponseConverter.toMultiResponse(listResult)
                : MultiResponse.of(activitiesResponses, listResult.getTotal());
    }
    
    // 发布活动
    @PostMapping
    public Response publishActivity(@RequestAttribute Long userId,
                                    @RequestBody PublishActivityRequest publishActivityRequest) {
        // 将请求参数转换为发布活动命令对象，并调用应用层发布活动
        PublishActivityCommand publishActivityCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult publishResult = saleActivityAppService.publishActivity(userId, publishActivityCommand);
        // 返回发布活动结果的响应
        return ResponseConverter.toResponse(publishResult);
    }
    
    // 修改活动
    @PutMapping("/{activityId}")
    public Response modifyActivity(@RequestAttribute Long userId,
                                   @PathVariable Long activityId,
                                   @RequestBody PublishActivityRequest publishActivityRequest) {
        // 将请求参数转换为修改活动命令对象，并调用应用层修改活动
        PublishActivityCommand modifyActivityCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult modifyResult = saleActivityAppService.modifyActivity(userId, activityId, modifyActivityCommand);
        // 返回修改活动结果的响应
        return ResponseConverter.toResponse(modifyResult);
    }
    
    // 上线活动
    @PutMapping("/{activityId}/online")
    public Response onlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        // 调用应用层上线活动
        AppResult onlineResult = saleActivityAppService.onlineActivity(userId, activityId);
        // 返回上线活动结果的响应
        return ResponseConverter.toResponse(onlineResult);
    }
    
    // 下线活动
    @PutMapping("/{activityId}/offline")
    public Response offlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        // 调用应用层下线活动
        AppResult offlineResult = saleActivityAppService.offlineActivity(userId, activityId);
        // 返回下线活动结果的响应
        return ResponseConverter.toResponse(offlineResult);
    }
}
