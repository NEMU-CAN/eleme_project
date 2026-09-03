package com.iteleme.backend.controller;

import com.iteleme.backend.entity.Result;
import com.iteleme.backend.service.OrderService;
import com.iteleme.backend.vo.request.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单接口。
 */
@RestController
@RequestMapping("/api/users/{userId}/orders")
public class OrderController {
    /** 订单业务服务。 */
    @Autowired
    private OrderService orderService;

    /**
     * 查询用户订单列表。
     */
    @GetMapping
    public Result listOrdersByUserId(@PathVariable("userId") String userId,
                                     @RequestParam(value = "businessId", required = false) Integer businessId,
                                     @RequestParam(value = "orderState", required = false) Integer orderState) {
        return Result.success(orderService.listOrdersByUserId(userId, businessId, orderState));
    }

    /**
     * 创建订单。
     */
    @PostMapping
    public ResponseEntity<Result> createOrder(@PathVariable("userId") String userId,
                                              @RequestBody OrderCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Result.success(orderService.createOrder(userId, request)));
    }

    /**
     * 查询订单详情。
     */
    @GetMapping("/{orderId}")
    public Result getOrderById(@PathVariable("userId") String userId,
                               @PathVariable("orderId") Integer orderId) {
        return Result.success(orderService.getOrderById(userId, orderId));
    }

    /**
     * 支付订单。
     *
     * <p>当前项目不接入真实支付渠道，用户发起请求后直接将订单标记为已支付。</p>
     */
    @PostMapping("/{orderId}/payments")
    public Result payOrder(@PathVariable("userId") String userId,
                           @PathVariable("orderId") Integer orderId) {
        return Result.success(orderService.payOrder(userId, orderId));
    }
}
