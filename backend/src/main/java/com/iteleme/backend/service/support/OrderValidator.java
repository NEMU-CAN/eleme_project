package com.iteleme.backend.service.support;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.CartMapper;
import com.iteleme.backend.mapper.DeliveryAddressMapper;
import com.iteleme.backend.vo.request.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// ============================================================
// [重构] 订单下单前置校验器：加载并校验商家/地址/购物车，返回上下文
// ============================================================
@Component
@RequiredArgsConstructor
public class OrderValidator {
    /** 商家表数据访问对象。 */
    private final BusinessMapper businessMapper;
    /** 送货地址表数据访问对象。 */
    private final DeliveryAddressMapper deliveryAddressMapper;
    /** 购物车表数据访问对象。 */
    private final CartMapper cartMapper;

    /**
     * 校验并加载下单所需数据（商家、收货地址、购物车），返回上下文。
     */
    public OrderContext validate(String userId, OrderCreateRequest request) {
        Business business = businessMapper.findById(request.getBusinessId());
        DeliveryAddress address = deliveryAddressMapper.findByIdForUser(userId, request.getDaId());
        if (business == null || address == null) {
            throw ApiException.notFound();
        }
        List<Cart> cartItems = cartMapper.findByUserId(userId, request.getBusinessId());
        if (cartItems.isEmpty()) {
            throw ApiException.conflict("cart", "购物车为空，无法创建订单");
        }
        return new OrderContext(business, address, cartItems);
    }
}
