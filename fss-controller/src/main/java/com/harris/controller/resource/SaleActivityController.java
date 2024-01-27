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
public class SaleActivityController {
    @Resource
    private SaleActivityAppService saleActivityAppService;

    @GetMapping("/sale-activities/{activityId}")
    public SingleResponse<SaleActivityResponse> getActivity(@RequestAttribute Long userId,
                                                            @PathVariable Long activityId,
                                                            @RequestParam(required = false) Long version) {

        AppSingleResult<SaleActivityDTO> activityResult = saleActivityAppService
                .getActivity(userId, activityId, version);

        SaleActivityDTO activityDTO = activityResult.getData();
        SaleActivityResponse activityResponse = SaleActivityConverter.toResponse(activityDTO);

        return !activityResult.isSuccess() || activityDTO == null
                ? ResponseConverter.toSingleResponse(activityResult)
                : SingleResponse.of(activityResponse);
    }

    // TODO: combine listActivities and listOnlineActivities into one using status
    @GetMapping(value = "/sale-activities")
    public MultiResponse<SaleActivityResponse> listActivities(@RequestAttribute Long userId,
                                                              @RequestParam Integer pageSize,
                                                              @RequestParam Integer pageNumber,
                                                              @RequestParam(required = false) String keyword) {

        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);

        AppMultiResult<SaleActivityDTO> activitiesResult = saleActivityAppService
                .listActivities(userId, activitiesQuery);

        Collection<SaleActivityDTO> activityDTOs = activitiesResult.getData();
        Collection<SaleActivityResponse> activitiesResponses = SaleActivityConverter.toResponseList(activityDTOs);

        return !activitiesResult.isSuccess() || activityDTOs == null
                ? ResponseConverter.toMultiResponse(activitiesResult)
                : MultiResponse.of(activitiesResponses, activitiesResult.getTotal());
    }

    @GetMapping(value = "/sale-activities/online")
    public MultiResponse<SaleActivityResponse> listOnlineActivities(@RequestAttribute Long userId,
                                                                    @RequestParam Integer pageSize,
                                                                    @RequestParam Integer pageNumber,
                                                                    @RequestParam(required = false) String keyword) {

        SaleActivitiesQuery activitiesQuery = new SaleActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(SaleActivityStatus.ONLINE.getCode());

        AppMultiResult<SaleActivityDTO> activitiesResult = saleActivityAppService
                .listActivities(userId, activitiesQuery);

        Collection<SaleActivityDTO> activityDTOs = activitiesResult.getData();
        Collection<SaleActivityResponse> activitiesResponses = SaleActivityConverter.toResponseList(activityDTOs);

        return !activitiesResult.isSuccess() || activityDTOs == null
                ? ResponseConverter.toMultiResponse(activitiesResult)
                : MultiResponse.of(activitiesResponses, activitiesResult.getTotal());
    }

    @PostMapping(value = "/sale-activities")
    public Response publishActivity(@RequestAttribute Long userId,
                                    @RequestBody PublishActivityRequest publishActivityRequest) {

        PublishActivityCommand activityPublishCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult publishResult = saleActivityAppService.publishActivity(userId, activityPublishCommand);

        return ResponseConverter.toResponse(publishResult);
    }

    @PutMapping(value = "/sale-activities/{activityId}")
    public Response modifyActivity(@RequestAttribute Long userId,
                                   @PathVariable Long activityId,
                                   @RequestBody PublishActivityRequest publishActivityRequest) {

        PublishActivityCommand publishActivityCommand = SaleActivityConverter.toCommand(publishActivityRequest);
        AppResult modifyResult = saleActivityAppService.modifyActivity(userId, activityId, publishActivityCommand);

        return ResponseConverter.toResponse(modifyResult);
    }

    // TODO: combine onlineActivity and offlineActivity into one using status
    @PutMapping(value = "/sale-activities/{activityId}/online")
    public Response onlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult onlineResult = saleActivityAppService.onlineActivity(userId, activityId);

        return ResponseConverter.toResponse(onlineResult);
    }

    @PutMapping(value = "/sale-activities/{activityId}/offline")
    public Response offlineActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult offlineResult = saleActivityAppService.offlineActivity(userId, activityId);

        return ResponseConverter.toResponse(offlineResult);
    }
}
