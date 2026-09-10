package com.iteleme.backend.vo;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.Food;

public record CartItemVO(
        Cart cart,
        Business business,
        Food food
) {
}
