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
import com.harris.app.service.app.FssActivityAppService;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.converter.SaleActivityConverter;
import com.harris.controller.model.request.PublishActivityRequest;
import com.harris.controller.model.response.SaleActivityResponse;
import com.harris.domain.model.enums.SaleActivityStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
public class FssActivityController {
    @Resource
    private FssActivityAppService fssActivityAppService;

    @GetMapping("/flash-activities/{activityId}")
    public SingleResponse<SaleActivityResponse> getActivity(@RequestAttribute Long userId,
                                                            @PathVariable Long activityId,
                                                            @RequestParam(required = false) Long version) {
        AppSingleResult<SaleActivityDTO> getResult = fssActivityAppService.getActivity(userId, activityId, version);
        SaleActivityDTO saleActivityDTO = getResult.getData();
        SaleActivityResponse saleActivityResponse = SaleActivityConverter.toResponse(saleActivityDTO);

        return !getResult.isSuccess() || saleActivityDTO == null
                ? ResponseConverter.toSingleResponse(getResult)
                : SingleResponse.of(saleActivityResponse);
    }

    @GetMapping(value = "/flash-activities")
    public MultiResponse<SaleActivityResponse> listActivities(@RequestAttribute Long userId,
                                                              @RequestParam Integer pageSize,
                                                              @RequestParam Integer pageNumber,
                                                              @RequestParam(required = false) String keyword) {
        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        AppMultiResult<SaleActivityDTO> listResult = fssActivityAppService.listActivities(userId, activitiesQuery);
        return ResponseConverter.toMultiResponse(listResult);
    }

    @GetMapping(value = "/flash-activities/online")
    public MultiResponse<SaleActivityResponse> listOnlineActivities(@RequestAttribute Long userId,
                                                                    @RequestParam Integer pageSize,
                                                                    @RequestParam Integer pageNumber,
                                                                    @RequestParam(required = false) String keyword) {
        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(SaleActivityStatus.ONLINE.getCode());
        AppMultiResult<SaleActivityDTO> listResult = fssActivityAppService.listActivities(userId, activitiesQuery);
        Collection<SaleActivityDTO> saleActivityDTOs = listResult.getData();
        Collection<SaleActivityResponse> saleActivityResponses = SaleActivityConverter.toResponseList(saleActivityDTOs);

        return !listResult.isSuccess() || saleActivityDTOs == null
                ? ResponseConverter.toMultiResponse(listResult)
                : MultiResponse.of(saleActivityResponses, listResult.getTotal());
    }

    @PostMapping(value = "/flash-activities")
    public Response publishActivity(@RequestAttribute Long userId,
                                    @RequestBody PublishActivityRequest publishActivityRequest) {
        PublishActivityCommand activityPublishCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult publishResult = fssActivityAppService.publishActivity(userId, activityPublishCommand);
        return ResponseConverter.toResponse(publishResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}")
    public Response modifyActivity(@RequestAttribute Long userId,
                                   @PathVariable Long activityId,
                                   @RequestBody PublishActivityRequest publishActivityRequest) {
        PublishActivityCommand publishActivityCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult modifyResult = fssActivityAppService.modifyActivity(userId, activityId, publishActivityCommand);
        return ResponseConverter.toResponse(modifyResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}/online")
    public Response onlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult onlineResult = fssActivityAppService.onlineActivity(userId, activityId);
        return ResponseConverter.toResponse(onlineResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}/offline")
    public Response offlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult offlineResult = fssActivityAppService.offlineActivity(userId, activityId);
        return ResponseConverter.toResponse(offlineResult);
    }
}
