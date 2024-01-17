package com.harris.controller.resource;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.FlashActivityPublishCommand;
import com.harris.app.model.dto.FlashActivityDTO;
import com.harris.app.model.query.FlashActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.FlashActivityAppService;
import com.harris.controller.model.converter.FlashActivityConverter;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.request.FlashActivityPublishRequest;
import com.harris.controller.model.response.FlashActivityResponse;
import com.harris.domain.model.enums.FlashActivityStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class FlashActivityController {
    @Resource
    private FlashActivityAppService flashActivityAppService;

    @GetMapping("/flash-activities/{activityId}")
    public SingleResponse<FlashActivityResponse> getFlashActivity(@RequestAttribute Long userId,
                                                                  @PathVariable Long activityId,
                                                                  @RequestParam(required = false) Long version) {
        AppSingleResult<FlashActivityDTO> flashActivityResult = flashActivityAppService
                .getFlashActivity(userId, activityId, version);
        if (!flashActivityResult.isSuccess() || flashActivityResult.getData() == null) {
            return ResponseConverter.withSingle(flashActivityResult);
        }
        FlashActivityDTO flashActivityDTO = flashActivityResult.getData();
        return SingleResponse.of(FlashActivityConverter.toResponse(flashActivityDTO));
    }

    @GetMapping(value = "/flash-activities")
    public MultiResponse<FlashActivityResponse> getFlashActivities(@RequestAttribute Long userId,
                                                                   @RequestParam Integer pageSize,
                                                                   @RequestParam Integer pageNumber,
                                                                   @RequestParam(required = false) String keyword) {
        FlashActivitiesQuery flashActivitiesQuery = new FlashActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        AppMultiResult<FlashActivityDTO> flashActivitiesResult = flashActivityAppService.getFlashActivities(userId, flashActivitiesQuery);
        return ResponseConverter.withMulti(flashActivitiesResult);
    }

    @GetMapping(value = "/flash-activities/online")
    public MultiResponse<FlashActivityResponse> getOnlineFlashActivities(@RequestAttribute Long userId,
                                                                         @RequestParam Integer pageSize,
                                                                         @RequestParam Integer pageNumber,
                                                                         @RequestParam(required = false) String keyword) {
        FlashActivitiesQuery flashActivitiesQuery = new FlashActivitiesQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(FlashActivityStatus.ONLINE.getCode());
        AppMultiResult<FlashActivityDTO> flashActivitiesResult = flashActivityAppService.getFlashActivities(userId, flashActivitiesQuery);
        if (!flashActivitiesResult.isSuccess() || flashActivitiesResult.getData() == null) {
            return ResponseConverter.withMulti(flashActivitiesResult);
        }
        return MultiResponse.of(FlashActivityConverter.toResponse(flashActivitiesResult.getData()), flashActivitiesResult.getTotal());
    }

    @PostMapping(value = "/flash-activities")
    public Response publishFlashActivity(@RequestAttribute Long userId, @RequestBody FlashActivityPublishRequest flashActivityPublishRequest) {
        FlashActivityPublishCommand activityPublishCommand = FlashActivityConverter.toCommand(flashActivityPublishRequest);
        AppResult appResult = flashActivityAppService.publishFlashActivity(userId, activityPublishCommand);
        return ResponseConverter.with(appResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}")
    public Response modifyFlashActivity(@RequestAttribute Long userId, @PathVariable Long activityId, @RequestBody FlashActivityPublishRequest flashActivityPublishRequest) {
        FlashActivityPublishCommand activityPublishCommand = FlashActivityConverter.toCommand(flashActivityPublishRequest);
        AppResult appResult = flashActivityAppService.modifyFlashActivity(userId, activityId, activityPublishCommand);
        return ResponseConverter.with(appResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}/online")
    public Response onlineFlashActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult appResult = flashActivityAppService.onlineFlashActivity(userId, activityId);
        return ResponseConverter.with(appResult);
    }

    @PutMapping(value = "/flash-activities/{activityId}/offline")
    public Response offlineFlashActivity(@RequestAttribute Long userId, @PathVariable Long activityId) {
        AppResult appResult = flashActivityAppService.offlineFlashActivity(userId, activityId);
        return ResponseConverter.with(appResult);
    }
}
