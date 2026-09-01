package com.iteleme.backend.controller;

import com.iteleme.backend.service.CartService;
import com.iteleme.backend.vo.CartItemVO;
import com.iteleme.backend.vo.request.CartCreateRequest;
import com.iteleme.backend.vo.request.CartUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/users/{userId}/cart-items")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public List<CartItemVO> listUserCartItems(@PathVariable("userId") String userId,
                                              @RequestParam(value = "businessId", required = false) Integer businessId) {
        return cartService.listCartItems(userId, businessId);
    }

    @PostMapping
    public CartItemVO upsertCartItem(@PathVariable("userId") String userId,
                                     @RequestBody CartCreateRequest request) {
        return cartService.upsertCartItem(userId, request);
    }

    @PatchMapping("/{cartId}")
    public CartItemVO updateCartItemQuantity(@PathVariable("userId") String userId,
                                             @PathVariable("cartId") Integer cartId,
                                             @RequestBody CartUpdateRequest request) {
        return cartService.updateCartItemQuantity(userId, cartId, request);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCartItemsByFilter(@PathVariable("userId") String userId,
                                                        @RequestParam(value = "businessId", required = false) Integer businessId,
                                                        @RequestParam(value = "foodId", required = false) Integer foodId) {
        cartService.deleteCartItemsByFilter(userId, businessId, foodId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable("userId") String userId,
                                               @PathVariable("cartId") Integer cartId) {
        cartService.deleteCartItem(userId, cartId);
        return ResponseEntity.noContent().build();
    }
}
