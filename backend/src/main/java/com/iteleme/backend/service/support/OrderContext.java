package com.iteleme.backend.service.support;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.DeliveryAddress;

import java.util.List;

// ============================================================
// [重构] 下单上下文：打包加载好的商家/地址/购物车，一次查询多处复用
// ============================================================
public record OrderContext(Business business, DeliveryAddress address, List<Cart> cartItems) {
}
