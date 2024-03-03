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
        // 从应用层获取商品详情
        AppSingleResult<SaleItemDTO> itemResult = saleItemAppService.getItem(userId, activityId, itemId, version);
        
        // 将商品详情转换为响应对象
        SaleItemDTO itemDTO = itemResult.getData();
        SaleItemResponse itemResponse = SaleItemConverter.toResponse(itemDTO);
        
        // 如果获取商品详情失败或者商品详情为空，则返回对应的错误响应；否则返回成功响应并携带商品详情
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
        // 构建查询对象
        SaleItemsQuery itemsQuery = new SaleItemsQuery().setKeyword(keyword).setPageSize(pageSize).setPageNumber(pageNumber);
        
        // 从应用层获取商品列表
        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);
        
        // 将商品列表转换为响应对象
        Collection<SaleItemDTO> itemDTOS = itemsResult.getData();
        Collection<SaleItemResponse> itemResponses = SaleItemConverter.toResponseList(itemDTOS);
        
        // 如果获取商品列表失败或者商品列表为空，则返回对应的错误响应；否则返回成功响应并携带商品列表
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
        // 构建查询对象
        SaleItemsQuery itemsQuery = new SaleItemsQuery()
                .setKeyword(keyword)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setStatus(SaleItemStatus.ONLINE.getCode());
        
        // 从应用层获取商品列表
        AppMultiResult<SaleItemDTO> itemsResult = saleItemAppService.listItems(userId, activityId, itemsQuery);
        
        // 将商品列表转换为响应对象
        Collection<SaleItemDTO> itemDTOS = itemsResult.getData();
        Collection<SaleItemResponse> itemResponses = SaleItemConverter.toResponseList(itemDTOS);
        
        // 如果获取商品列表失败或者商品列表为空，则返回对应的错误响应；否则返回成功响应并携带商品列表
        return !itemsResult.isSuccess() || itemDTOS == null
                ? ResponseConverter.toMultiResponse(itemsResult)
                : MultiResponse.of(itemResponses, itemsResult.getTotal());
    }
    
    // 发布商品
    @PostMapping
    public Response publishItem(@RequestAttribute Long userId,
                                @PathVariable Long activityId,
                                @RequestBody PublishItemRequest publishItemRequest) {
        // 将请求参数转换为发布商品命令对象，并调用应用层发布商品
        PublishItemCommand publishItemCommand = SaleItemConverter.toCommand(publishItemRequest);
        AppResult publishResult = saleItemAppService.publishItem(userId, activityId, publishItemCommand);
        // 返回发布商品结果的响应
        return ResponseConverter.toResponse(publishResult);
    }
    
    // 修改商品
    @PutMapping("/{itemId}/online")
    public Response onlineItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        // 调用应用层上线商品
        AppResult onlineResult = saleItemAppService.onlineItem(userId, activityId, itemId);
        // 返回上线商品结果的响应
        return ResponseConverter.toResponse(onlineResult);
    }
    
    @PutMapping("/{itemId}/offline")
    public Response offlineItem(@RequestAttribute Long userId, @PathVariable Long activityId, @PathVariable Long itemId) {
        // 调用应用层下线商品
        AppResult offlineResult = saleItemAppService.offlineItem(userId, activityId, itemId);
        // 返回下线商品结果的响应
        return ResponseConverter.toResponse(offlineResult);
    }
}
