package com.harris.controller.resource;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.app.FssItemAppService;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.converter.SaleItemConverter;
import com.harris.controller.model.request.PublishItemRequest;
import com.harris.controller.model.response.SaleItemResponse;
import com.harris.domain.model.enums.SaleItemStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
public class FssItemController {
    @Resource
    private FssItemAppService fssItemAppService;

    @GetMapping(value = "/activities/{activityId}/flash-items/{itemId}")
    public SingleResponse<SaleItemResponse> getItem(@RequestAttribute Long userId,
                                                    @PathVariable Long activityId,
                                                    @PathVariable Long itemId,
                                                    @RequestParam(required = false) Long version) {
        AppSingleResult<SaleItemDTO> getResult = fssItemAppService.getItem(userId, activityId, itemId, version);
        SaleItemDTO saleItemDTO = getResult.getData();
        SaleItemResponse saleItemResponse = SaleItemConverter.toResponse(saleItemDTO);

        return !getResult.isSuccess() || saleItemDTO == null
                ? ResponseConverter.toSingleResponse(getResult)
                : SingleResponse.of(saleItemResponse);
    }

    @GetMapping(value = "/activities/{activityId}/flash-items")
    public MultiResponse<SaleItemDTO> listItems(@RequestAttribute Long userId,
                                                @PathVariable Long activityId,
                                                @RequestParam Integer pageSize,
                                                @RequestParam Integer pageNumber,
                                                @RequestParam(required = false) String keyword) {
        SaleItemsQuery saleItemsQuery = new SaleItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        AppMultiResult<SaleItemDTO> listResult = fssItemAppService.listItems(userId, activityId, saleItemsQuery);
        return ResponseConverter.toMultiResponse(listResult);
    }

    @GetMapping(value = "/activities/{activityId}/flash-items/online")
    public MultiResponse<SaleItemResponse> listOnlineItems(@RequestAttribute Long userId,
                                                           @PathVariable Long activityId,
                                                           @RequestParam Integer pageSize,
                                                           @RequestParam Integer pageNumber,
                                                           @RequestParam(required = false) String keyword) {
        SaleItemsQuery saleItemsQuery = new SaleItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(SaleItemStatus.ONLINE.getCode());
        AppMultiResult<SaleItemDTO> listResult = fssItemAppService.listItems(userId, activityId, saleItemsQuery);
        Collection<SaleItemDTO> saleItemDTOS = listResult.getData();
        Collection<SaleItemResponse> saleItemResponses = SaleItemConverter.toResponseList(saleItemDTOS);

        return !listResult.isSuccess() || saleItemDTOS == null
                ? ResponseConverter.toMultiResponse(listResult)
                : MultiResponse.of(saleItemResponses, listResult.getTotal());
    }

    @PostMapping(value = "/activities/{activityId}/flash-items")
    public Response publishItem(@RequestAttribute Long userId,
                                @PathVariable Long activityId,
                                @RequestBody PublishItemRequest publishItemRequest) {
        PublishItemCommand publishItemCommand = SaleItemConverter.toCommand(publishItemRequest);
        AppResult publishResult = fssItemAppService.publishItem(userId, activityId, publishItemCommand);
        return ResponseConverter.toResponse(publishResult);
    }

    @PutMapping(value = "/activities/{activityId}/flash-items/{itemId}/online")
    public Response onlineItem(@RequestAttribute Long userId,
                               @PathVariable Long activityId,
                               @PathVariable Long itemId) {
        AppResult onlineResult = fssItemAppService.onlineItem(userId, activityId, itemId);
        return ResponseConverter.toResponse(onlineResult);
    }

    @PutMapping(value = "/activities/{activityId}/flash-items/{itemId}/offline")
    public Response offlineItem(@RequestAttribute Long userId,
                                @PathVariable Long activityId,
                                @PathVariable Long itemId) {
        AppResult offlineResult = fssItemAppService.offlineItem(userId, activityId, itemId);
        return ResponseConverter.toResponse(offlineResult);
    }
}
