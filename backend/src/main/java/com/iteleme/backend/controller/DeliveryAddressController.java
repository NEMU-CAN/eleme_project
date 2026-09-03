package com.iteleme.backend.controller;

import com.iteleme.backend.entity.Result;
import com.iteleme.backend.service.DeliveryAddressService;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户维度的收货地址 RESTful 接口。 */
@RestController
@RequestMapping("/api/users/{userId}/delivery-addresses")
public class DeliveryAddressController {
    private final DeliveryAddressService deliveryAddressService;

    public DeliveryAddressController(DeliveryAddressService deliveryAddressService) {
        this.deliveryAddressService = deliveryAddressService;
    }

    /** 查询指定用户的全部收货地址。 */
    @GetMapping
    public Result list(@PathVariable String userId) {
        return Result.success(deliveryAddressService.listDeliveryAddressesByUserId(userId));
    }

    /** 为指定用户新增收货地址。 */
    @PostMapping
    public ResponseEntity<Result> create(@PathVariable String userId,
                                         @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(deliveryAddressService.createDeliveryAddress(userId, request)));
    }

    /** 获取属于指定用户的收货地址详情。 */
    @GetMapping("/{daId}")
    public Result get(@PathVariable String userId,
                      @PathVariable Integer daId) {
        return Result.success(deliveryAddressService.getDeliveryAddressById(userId, daId));
    }

    /** 全量更新属于指定用户的收货地址。 */
    @PutMapping("/{daId}")
    public Result update(@PathVariable String userId,
                         @PathVariable Integer daId,
                         @RequestBody DeliveryAddressRequest request) {
        return Result.success(deliveryAddressService.updateDeliveryAddress(userId, daId, request));
    }

    /** 删除属于指定用户的收货地址。 */
    @DeleteMapping("/{daId}")
    public Result delete(@PathVariable String userId,
                         @PathVariable Integer daId) {
        deliveryAddressService.deleteDeliveryAddress(userId, daId);
        return Result.success();
    }
}
