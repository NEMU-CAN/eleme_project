package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.dto.CartItemSaveRequest;
import com.iteleme.backend.dto.CartItemUpdateRequest;
import com.iteleme.backend.service.CartService;
import com.iteleme.backend.support.RequestValues;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/cart/items")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public Result list(@RequestParam(required = false) Integer businessId,
                       @RequestParam(name = "business_id", required = false) Integer businessIdSnake) {
        return Result.success(cartService.list(RequestValues.first(businessId, businessIdSnake)));
    }

    @PostMapping
    public Result add(@RequestBody @Valid CartItemSaveRequest request) {
        return Result.success(cartService.add(request));
    }

    @PutMapping("/{foodId}")
    public Result update(@PathVariable Integer foodId, @RequestBody @Valid CartItemUpdateRequest request) {
        return Result.success(cartService.update(foodId, request));
    }

    @DeleteMapping("/{foodId}")
    public Result remove(@PathVariable Integer foodId) {
        cartService.remove(foodId);
        return Result.success();
    }

    @DeleteMapping
    public Result clear(@RequestParam(required = false) Integer businessId,
                        @RequestParam(name = "business_id", required = false) Integer businessIdSnake) {
        cartService.clear(RequestValues.first(businessId, businessIdSnake));
        return Result.success();
    }
}
