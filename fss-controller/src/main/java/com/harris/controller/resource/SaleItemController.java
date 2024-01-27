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
import com.harris.app.service.app.SaleItemAppService;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.converter.SaleItemConverter;
import com.harris.controller.model.request.PublishItemRequest;
import com.harris.controller.model.response.SaleItemResponse;
import com.harris.domain.model.enums.SaleItemStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;

@RestController
public class SaleItemController {
    @Resource
    private SaleItemAppService saleItemAppService;

    @GetMapping(value = "/sale-activities/{activityId}/sale-items/{itemId}")
    public SingleResponse<SaleItemResponse> getItem(@RequestAttribute Long userId,
                                                    @PathVariable Long activityId,
                                                    @PathVariable Long itemId,
                                                    @RequestParam(required = false) Long version) {

        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(userId, activityId, itemId, version);

        SaleItemDTO itemDTO = itemResult.getData();
        SaleItemResponse itemResponse = SaleItemConverter.toResponse(itemDTO);

        return !itemResult.isSuccess() || itemDTO == null
                ? ResponseConverter.toSingleResponse(itemResult)
                : SingleResponse.of(itemResponse);
    }

    // TODO: combine listItems and listOnlineItems into one using status
    @GetMapping(value = "/sale-activities/{activityId}/sale-items")
    public MultiResponse<SaleItemResponse> listItems(@RequestAttribute Long userId,
                                                     @PathVariable Long activityId,
                                                     @RequestParam Integer pageSize,
                                                     @RequestParam Integer pageNumber,
                                                     @RequestParam(required = false) String keyword) {

        SaleItemsQuery itemsQuery = new SaleItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);

        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);

        Collection<SaleItemDTO> itemDTOS = itemsResult.getData();
        Collection<SaleItemResponse> itemResponses = SaleItemConverter.toResponseList(itemDTOS);

        return !itemsResult.isSuccess() || itemDTOS == null
                ? ResponseConverter.toMultiResponse(itemsResult)
                : MultiResponse.of(itemResponses, itemsResult.getTotal());
    }

    @GetMapping(value = "/activities/{activityId}/sale-items/online")
    public MultiResponse<SaleItemResponse> listOnlineItems(@RequestAttribute Long userId,
                                                           @PathVariable Long activityId,
                                                           @RequestParam Integer pageSize,
                                                           @RequestParam Integer pageNumber,
                                                           @RequestParam(required = false) String keyword) {

        SaleItemsQuery itemsQuery = new SaleItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(SaleItemStatus.ONLINE.getCode());

        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);

        Collection<SaleItemDTO> itemDTOS = itemsResult.getData();
        Collection<SaleItemResponse> itemResponses = SaleItemConverter.toResponseList(itemDTOS);

        return !itemsResult.isSuccess() || itemDTOS == null
                ? ResponseConverter.toMultiResponse(itemsResult)
                : MultiResponse.of(itemResponses, itemsResult.getTotal());
    }

    @PostMapping(value = "/activities/{activityId}/sale-items")
    public Response publishItem(@RequestAttribute Long userId,
                                @PathVariable Long activityId,
                                @RequestBody PublishItemRequest publishItemRequest) {

        PublishItemCommand publishItemCommand = SaleItemConverter.toCommand(publishItemRequest);
        AppResult publishResult = saleItemAppService.publishItem(userId, activityId, publishItemCommand);

        return ResponseConverter.toResponse(publishResult);
    }

    // TODO: combine onlineItem and offlineItem into one using status
    @PutMapping(value = "/activities/{activityId}/sale-items/{itemId}/online")
    public Response onlineItem(@RequestAttribute Long userId,
                               @PathVariable Long activityId,
                               @PathVariable Long itemId) {

        AppResult onlineResult = saleItemAppService.onlineItem(userId, activityId, itemId);

        return ResponseConverter.toResponse(onlineResult);
    }

    @PutMapping(value = "/activities/{activityId}/sale-items/{itemId}/offline")
    public Response offlineItem(@RequestAttribute Long userId,
                                @PathVariable Long activityId,
                                @PathVariable Long itemId) {

        AppResult offlineResult = saleItemAppService.offlineItem(userId, activityId, itemId);

        return ResponseConverter.toResponse(offlineResult);
    }
}
