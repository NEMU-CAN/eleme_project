package com.iteleme.backend.controller;

import com.iteleme.backend.service.OrderService;
import com.iteleme.backend.vo.OrderVO;
import com.iteleme.backend.vo.request.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/users/{userId}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public List<OrderVO> listOrdersByUserId(@PathVariable("userId") String userId,
                                            @RequestParam(value = "businessId", required = false) Integer businessId,
                                            @RequestParam(value = "orderState", required = false) Integer orderState) {
        return orderService.listOrdersByUserId(userId, businessId, orderState);
    }

    @PostMapping
    public ResponseEntity<OrderVO> createOrder(@PathVariable("userId") String userId,
                                               @RequestBody OrderCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(userId, request));
    }

    @GetMapping("/{orderId}")
    public OrderVO getOrderById(@PathVariable("userId") String userId,
                                @PathVariable("orderId") Integer orderId) {
        return orderService.getOrderById(userId, orderId);
    }
}
