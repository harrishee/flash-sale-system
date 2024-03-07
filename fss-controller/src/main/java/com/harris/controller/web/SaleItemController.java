package com.harris.controller.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.saleitem.SaleItemAppService;
import com.harris.controller.model.converter.ResponseConverter;
import com.harris.controller.model.converter.SaleItemConverter;
import com.harris.controller.model.request.PublishItemRequest;
import com.harris.controller.model.response.SaleItemResponse;
import com.harris.domain.model.enums.SaleItemStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
        // 应用层 -> 本地缓存 -> 分布式缓存 -> 最新状态缓存 / 稍后再试（tryLater） / 不存在（notExist）
        // 高并发读商品请求被阻止在应用层，通过缓存实现数据获取，不会让请求进入领域层。
        // 只有在分布式 数据尚未被缓存 和 缓存失效 的情况下，才会进入领域层获取数据。
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(userId, activityId, itemId, version);
        if (!itemResult.isSuccess() || itemResult.getData() == null) {
            return ResponseConverter.toSingleResponse(itemResult);
        }
        
        SaleItemDTO itemDTO = itemResult.getData();
        SaleItemResponse itemResponse = SaleItemConverter.toResponse(itemDTO);
        return SingleResponse.of(itemResponse);
    }
    
    // 获取活动ID的 商品列表
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
        
        // 调用应用层的 获取商品列表 方法
        // 对第一页且无关键字且商品在线的查询，走缓存；其他情况走数据库
        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);
        if (!itemsResult.isSuccess() || itemsResult.getData() == null) {
            return ResponseConverter.toMultiResponse(itemsResult);
        }
        
        return MultiResponse.of(SaleItemConverter.toResponseList(itemsResult.getData()), itemsResult.getTotal());
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
        
        // 调用应用层的 获取商品列表 方法
        // 对第一页且无关键字且商品在线的查询，走缓存；其他情况走数据库
        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);
        if (!itemsResult.isSuccess() || itemsResult.getData() == null) {
            return ResponseConverter.toMultiResponse(itemsResult);
        }
        
        return MultiResponse.of(SaleItemConverter.toResponseList(itemsResult.getData()), itemsResult.getTotal());
    }
    
    // 发布商品
    @PostMapping
    public Response publishItem(@RequestAttribute Long userId,
                                @PathVariable Long activityId,
                                @RequestBody PublishItemRequest publishItemRequest) {
        // 调用应用层的 发布商品 方法
        // 应用层加分布式锁，用户防抖，key = ITEM_CREATE_LOCK_KEY + userId
        PublishItemCommand publishItemCommand = SaleItemConverter.toCommand(publishItemRequest);
        AppResult publishResult = saleItemAppService.publishItem(userId, activityId, publishItemCommand);
        return ResponseConverter.toResponse(publishResult);
    }
    
    // 上线商品
    @PutMapping("/{itemId}/online")
    public Response onlineItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        // 调用应用层的 上线商品 方法
        // 应用层加分布式锁，用户防抖，key = ITEM_MODIFICATION_LOCK_KEY + userId
        AppResult onlineResult = saleItemAppService.onlineItem(userId, activityId, itemId);
        return ResponseConverter.toResponse(onlineResult);
    }
    
    // 下线商品
    @PutMapping("/{itemId}/offline")
    public Response offlineItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        // 调用应用层的 下线商品 方法
        // 应用层加分布式锁，用户防抖，key = ITEM_MODIFICATION_LOCK_KEY + userId
        AppResult offlineResult = saleItemAppService.offlineItem(userId, activityId, itemId);
        return ResponseConverter.toResponse(offlineResult);
    }
}
