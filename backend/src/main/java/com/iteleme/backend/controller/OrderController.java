package com.iteleme.backend.controller;

import com.iteleme.backend.common.PageResult;
import com.iteleme.backend.common.Result;
import com.iteleme.backend.dto.OrderCreateRequest;
import com.iteleme.backend.dto.OrderStatusRequest;
import com.iteleme.backend.service.OrderService;
import com.iteleme.backend.support.RequestValues;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Result> create(@RequestBody @Valid OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(orderService.create(request)));
    }

    @GetMapping
    public Result list(@RequestParam(required = false) Integer businessId,
                       @RequestParam(name = "business_id", required = false) Integer businessIdSnake,
                       @RequestParam(required = false) Integer orderStatus,
                       @RequestParam(name = "order_status", required = false) Integer orderStatusSnake,
                       @RequestParam(name = "orderState", required = false) Integer orderState,
                       @RequestParam(required = false, defaultValue = "1") Integer page,
                       @RequestParam(name = "page_size", required = false, defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer pageSizeCamel) {
        Integer resolvedBusinessId = RequestValues.first(businessId, businessIdSnake);
        Integer resolvedOrderStatus = RequestValues.first(RequestValues.first(orderStatus, orderStatusSnake), orderState);
        Integer resolvedPageSize = RequestValues.first(pageSizeCamel, pageSize);
        PageResult<?> result = orderService.list(resolvedBusinessId, resolvedOrderStatus, page, resolvedPageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Integer id) {
        return Result.success(orderService.get(id));
    }

    @PutMapping("/{id}/status")
    public Result status(@PathVariable Integer id, @RequestBody @Valid OrderStatusRequest request) {
        orderService.status(id, request);
        return Result.success();
    }
}
