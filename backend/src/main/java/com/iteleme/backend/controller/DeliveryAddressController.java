package com.iteleme.backend.controller;

import com.iteleme.backend.entity.Result;
import com.iteleme.backend.service.DeliveryAddressService;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

/**
 * 送货地址接口。
 */
@RestController
@RequestMapping("/api/users/{userId}/delivery-addresses")
public class DeliveryAddressController {
    /** 送货地址业务服务。 */
    @Autowired
    private DeliveryAddressService deliveryAddressService;

    /**
     * 查询用户地址列表。
     */
    @GetMapping
    public Result listDeliveryAddressesByUserId(@PathVariable("userId") String userId) {
        return Result.success(deliveryAddressService.listDeliveryAddressesByUserId(userId));
    }

    /**
     * 新增送货地址。
     */
    @PostMapping
    public ResponseEntity<Result> createDeliveryAddress(@PathVariable("userId") String userId,
                                                        @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Result.success(deliveryAddressService.createDeliveryAddress(userId, request)));
    }

    /**
     * 查询指定送货地址。
     */
    @GetMapping("/{daId}")
    public Result getDeliveryAddressById(@PathVariable("userId") String userId,
                                         @PathVariable("daId") Integer daId) {
        return Result.success(deliveryAddressService.getDeliveryAddressById(userId, daId));
    }

    /**
     * 修改送货地址。
     */
    @PutMapping("/{daId}")
    public Result updateDeliveryAddress(@PathVariable("userId") String userId,
                                        @PathVariable("daId") Integer daId,
                                        @RequestBody DeliveryAddressRequest request) {
        return Result.success(deliveryAddressService.updateDeliveryAddress(userId, daId, request));
    }

    /**
     * 删除送货地址。
     */
    @DeleteMapping("/{daId}")
    public Result deleteDeliveryAddress(@PathVariable("userId") String userId,
                                        @PathVariable("daId") Integer daId) {
        deliveryAddressService.deleteDeliveryAddress(userId, daId);
        return Result.success();
    }
}
