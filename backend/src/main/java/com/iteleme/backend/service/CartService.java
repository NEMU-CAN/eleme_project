package com.iteleme.backend.service;

import com.iteleme.backend.vo.CartItemVO;
import com.iteleme.backend.vo.request.CartCreateRequest;
import com.iteleme.backend.vo.request.CartUpdateRequest;

import java.util.List;

public interface CartService {
    List<CartItemVO> listCartItems(String userId, Integer businessId);

    CartItemVO upsertCartItem(String userId, CartCreateRequest request);

    CartItemVO updateCartItemQuantity(String userId, Integer cartId, CartUpdateRequest request);

    void deleteCartItemsByFilter(String userId, Integer businessId, Integer foodId);

    void deleteCartItem(String userId, Integer cartId);
}
