package com.iteleme.backend.controller;

import com.iteleme.backend.entity.Result;
import com.iteleme.backend.service.CartService;
import com.iteleme.backend.vo.request.CartCreateRequest;
import com.iteleme.backend.vo.request.CartUpdateRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 用户维度的购物车 RESTful 接口。 */
@RestController
@RequestMapping("/api/users/{userId}/cart-items")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** 查询某个用户的购物车，可按商家过滤。 */
    @GetMapping
    public Result list(@PathVariable String userId,
                       @RequestParam(required = false) Integer businessId) {
        return Result.success(cartService.listCartItems(userId, businessId));
    }

    /** 将食品加入购物车；已存在时累加数量。 */
    @PostMapping
    public Result upsert(@PathVariable String userId,
                         @RequestBody CartCreateRequest request) {
        return Result.success(cartService.upsertCartItem(userId, request));
    }

    /** 更新指定购物车条目的数量。 */
    @PatchMapping("/{cartId}")
    public Result updateQuantity(@PathVariable String userId,
                                 @PathVariable Integer cartId,
                                 @RequestBody CartUpdateRequest request) {
        return Result.success(cartService.updateCartItemQuantity(userId, cartId, request));
    }

    /** 删除指定购物车条目。 */
    @DeleteMapping("/{cartId}")
    public Result deleteOne(@PathVariable String userId,
                            @PathVariable Integer cartId) {
        cartService.deleteCartItem(userId, cartId);
        return Result.success();
    }

    /** 按商家和食品条件批量删除用户购物车条目。 */
    @DeleteMapping
    public Result deleteByFilter(@PathVariable String userId,
                                 @RequestParam(required = false) Integer businessId,
                                 @RequestParam(required = false) Integer foodId) {
        cartService.deleteCartItemsByFilter(userId, businessId, foodId);
        return Result.success();
    }
}
