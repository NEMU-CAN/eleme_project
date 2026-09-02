package com.iteleme.backend.controller;

import com.iteleme.backend.entity.Result;
import com.iteleme.backend.service.CartService;
import com.iteleme.backend.vo.request.CartCreateRequest;
import com.iteleme.backend.vo.request.CartUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 购物车接口。
 */
@RestController
@RequestMapping("/api/users/{userId}/cart-items")
public class CartController {
    /** 购物车业务服务。 */
    @Autowired
    private CartService cartService;

    /**
     * 查询用户购物车列表。
     */
    @GetMapping
    public Result listUserCartItems(@PathVariable("userId") String userId,
                                    @RequestParam(value = "businessId", required = false) Integer businessId) {
        return Result.success(cartService.listCartItems(userId, businessId));
    }

    /**
     * 新增或累加购物车条目。
     */
    @PostMapping
    public Result upsertCartItem(@PathVariable("userId") String userId,
                                 @RequestBody CartCreateRequest request) {
        return Result.success(cartService.upsertCartItem(userId, request));
    }

    /**
     * 修改购物车条目数量。
     */
    @PatchMapping("/{cartId}")
    public Result updateCartItemQuantity(@PathVariable("userId") String userId,
                                         @PathVariable("cartId") Integer cartId,
                                         @RequestBody CartUpdateRequest request) {
        return Result.success(cartService.updateCartItemQuantity(userId, cartId, request));
    }

    /**
     * 按条件删除购物车条目。
     */
    @DeleteMapping
    public Result deleteCartItemsByFilter(@PathVariable("userId") String userId,
                                          @RequestParam(value = "businessId", required = false) Integer businessId,
                                          @RequestParam(value = "foodId", required = false) Integer foodId) {
        cartService.deleteCartItemsByFilter(userId, businessId, foodId);
        return Result.success();
    }

    /**
     * 删除指定购物车条目。
     */
    @DeleteMapping("/{cartId}")
    public Result deleteCartItem(@PathVariable("userId") String userId,
                                 @PathVariable("cartId") Integer cartId) {
        cartService.deleteCartItem(userId, cartId);
        return Result.success();
    }
}
