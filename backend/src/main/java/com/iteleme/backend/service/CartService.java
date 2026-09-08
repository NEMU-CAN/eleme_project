package com.iteleme.backend.service;

import com.iteleme.backend.dto.CartItemSaveRequest;
import com.iteleme.backend.dto.CartItemUpdateRequest;
import com.iteleme.backend.vo.CartItemVO;

import java.util.List;

public interface CartService {
    List<CartItemVO> list(Integer businessId);

    CartItemVO add(CartItemSaveRequest request);

    CartItemVO update(Integer foodId, CartItemUpdateRequest request);

    void remove(Integer foodId);

    void clear(Integer businessId);
}
