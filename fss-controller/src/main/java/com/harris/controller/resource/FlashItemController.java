package com.harris.controller.resource;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.app.model.query.FlashItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.FlashItemAppService;
import com.harris.controller.model.converter.FlashItemConverter;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.request.FlashItemPublishRequest;
import com.harris.controller.model.response.FlashItemResponse;
import com.harris.domain.model.enums.FlashItemStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class FlashItemController {
    @Resource
    private FlashItemAppService flashItemAppService;

    @GetMapping(value = "/activities/{activityId}/flash-items/{itemId}")
    public SingleResponse<FlashItemResponse> getFlashItem(@RequestAttribute Long userId,
                                                          @PathVariable Long activityId,
                                                          @PathVariable Long itemId,
                                                          @RequestParam(required = false) Long version) {
        AppSingleResult<FlashItemDTO> flashItemResult = flashItemAppService.getFlashItem(userId, activityId, itemId, version);
        if (!flashItemResult.isSuccess() || flashItemResult.getData() == null) {
            return ResponseConverter.withSingle(flashItemResult);
        }
        return SingleResponse.of(FlashItemConverter.toResponse(flashItemResult.getData()));
    }

    @GetMapping(value = "/activities/{activityId}/flash-items")
    public MultiResponse<FlashItemDTO> getFlashItems(@RequestAttribute Long userId,
                                                     @PathVariable Long activityId,
                                                     @RequestParam Integer pageSize,
                                                     @RequestParam Integer pageNumber,
                                                     @RequestParam(required = false) String keyword) {
        FlashItemsQuery flashItemsQuery = new FlashItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        AppMultiResult<FlashItemDTO> flashItemsResult = flashItemAppService.getFlashItems(userId, activityId, flashItemsQuery);
        return ResponseConverter.withMulti(flashItemsResult);
    }

    @GetMapping(value = "/activities/{activityId}/flash-items/online")
    public MultiResponse<FlashItemResponse> getOnlineFlashItems(@RequestAttribute Long userId,
                                                                @PathVariable Long activityId,
                                                                @RequestParam Integer pageSize,
                                                                @RequestParam Integer pageNumber,
                                                                @RequestParam(required = false) String keyword) {
        FlashItemsQuery flashItemsQuery = new FlashItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(FlashItemStatus.ONLINE.getCode());
        AppMultiResult<FlashItemDTO> flashItemsResult = flashItemAppService.getFlashItems(userId, activityId, flashItemsQuery);
        if (!flashItemsResult.isSuccess() || flashItemsResult.getData() == null) {
            return ResponseConverter.withMulti(flashItemsResult);
        }
        return MultiResponse.of(FlashItemConverter.toResponses(flashItemsResult.getData()), flashItemsResult.getTotal());
    }

    @PostMapping(value = "/activities/{activityId}/flash-items")
    public Response publishFlashItem(@RequestAttribute Long userId, @PathVariable Long activityId, @RequestBody FlashItemPublishRequest flashItemPublishRequest) {
        AppResult publishResult = flashItemAppService.publishFlashItem(userId, activityId, FlashItemConverter.toCommand(flashItemPublishRequest));
        return ResponseConverter.with(publishResult);
    }

    @PutMapping(value = "/activities/{activityId}/flash-items/{itemId}/online")
    public Response onlineFlashItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        AppResult onlineResult = flashItemAppService.onlineFlashItem(userId, activityId, itemId);
        return ResponseConverter.with(onlineResult);
    }

    @PutMapping(value = "/activities/{activityId}/flash-items/{itemId}/offline")
    public Response offlineFlashItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        AppResult onlineResult = flashItemAppService.offlineFlashItem(userId, activityId, itemId);
        return ResponseConverter.with(onlineResult);
    }
}
