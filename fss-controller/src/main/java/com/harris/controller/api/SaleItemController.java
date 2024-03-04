package com.harris.controller.api;

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
@RequestMapping("/sale-activities/{activityId}/sale-items")
public class SaleItemController {
    @Resource
    private SaleItemAppService saleItemAppService;
    
    // 根据商品ID获取商品详情
    @GetMapping("/{itemId}")
    public SingleResponse<SaleItemResponse> getItem(@RequestAttribute Long userId,
                                                    @PathVariable Long activityId,
                                                    @PathVariable Long itemId,
                                                    @RequestParam(required = false) Long version) {
        // 调用应用层的 获取商品 方法
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(userId, activityId, itemId, version);
        SaleItemDTO itemDTO = itemResult.getData();
        SaleItemResponse itemResponse = SaleItemConverter.toResponse(itemDTO);
        
        // 检查获取商品是否 失败 或者 为空
        return !itemResult.isSuccess() || itemDTO == null
                ? ResponseConverter.toSingleResponse(itemResult)
                : SingleResponse.of(itemResponse);
    }
    
    // 获取商品列表
    @GetMapping
    public MultiResponse<SaleItemResponse> listItems(@RequestAttribute Long userId,
                                                     @PathVariable Long activityId,
                                                     @RequestParam Integer pageSize,
                                                     @RequestParam Integer pageNumber,
                                                     @RequestParam(required = false) String keyword) {
        SaleItemsQuery itemsQuery = new SaleItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber);
        
        // 调用应用层的 获取商品列表 方法（走的是数据库）
        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);
        Collection<SaleItemDTO> itemDTOS = itemsResult.getData();
        Collection<SaleItemResponse> itemResponses = SaleItemConverter.toResponseList(itemDTOS);
        
        // 检查获取商品列表是否 失败 或者 为空
        return !itemsResult.isSuccess() || itemDTOS == null
                ? ResponseConverter.toMultiResponse(itemsResult)
                : MultiResponse.of(itemResponses, itemsResult.getTotal());
    }
    
    // 获取在线商品列表
    @GetMapping("/online")
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
        
        // 调用应用层的 获取商品列表 方法（走的是数据库）
        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);
        Collection<SaleItemDTO> itemDTOS = itemsResult.getData();
        Collection<SaleItemResponse> itemResponses = SaleItemConverter.toResponseList(itemDTOS);
        
        // 检查获取商品列表是否 失败 或者 为空
        return !itemsResult.isSuccess() || itemDTOS == null
                ? ResponseConverter.toMultiResponse(itemsResult)
                : MultiResponse.of(itemResponses, itemsResult.getTotal());
    }
    
    // 发布商品
    @PostMapping
    public Response publishItem(@RequestAttribute Long userId,
                                @PathVariable Long activityId,
                                @RequestBody PublishItemRequest publishItemRequest) {
        // 调用应用层的 发布商品 方法
        PublishItemCommand publishItemCommand = SaleItemConverter.toCommand(publishItemRequest);
        AppResult publishResult = saleItemAppService.publishItem(userId, activityId, publishItemCommand);
        return ResponseConverter.toResponse(publishResult);
    }
    
    // 上线商品
    @PutMapping("/{itemId}/online")
    public Response onlineItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        // 调用应用层的 上线商品 方法
        AppResult onlineResult = saleItemAppService.onlineItem(userId, activityId, itemId);
        return ResponseConverter.toResponse(onlineResult);
    }
    
    // 下线商品
    @PutMapping("/{itemId}/offline")
    public Response offlineItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        // 调用应用层的 下线商品 方法
        AppResult offlineResult = saleItemAppService.offlineItem(userId, activityId, itemId);
        return ResponseConverter.toResponse(offlineResult);
    }
}
