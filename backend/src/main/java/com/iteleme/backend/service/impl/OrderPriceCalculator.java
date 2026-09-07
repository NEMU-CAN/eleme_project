package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.FoodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

// ============================================================
// [重构] 从 OrderServiceImpl 拆出：订单金额计算器
// 职责：计算菜品总价、校验起送门槛、计算订单总价（菜品总价 + 配送费）。
// ============================================================
@Component
@RequiredArgsConstructor
public class OrderPriceCalculator {
    /** 食品表数据访问对象。 */
    private final FoodMapper foodMapper;

    /**
     * 计算购物车菜品总价。
     */
    public BigDecimal foodTotal(List<Cart> cartItems, Integer businessId) {
        BigDecimal total = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            Food food = foodMapper.findByIdAndBusinessId(cartItem.getFoodId(), businessId);
            if (food == null) {
                throw ApiException.notFound();
            }
            total = total.add(food.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        return total;
    }

    /**
     * 校验起送门槛：菜品总价必须达到起送费。
     */
    public void ensureMeetStartPrice(Business business, BigDecimal foodTotal) {
        if (business.getStartPrice() != null && foodTotal.compareTo(business.getStartPrice()) < 0) {
            throw ApiException.conflict("businessId", "未达起送金额 " + business.getStartPrice());
        }
    }

    /**
     * 订单总价 = 菜品总价 + 配送费。
     */
    public BigDecimal orderTotal(Business business, BigDecimal foodTotal) {
        return foodTotal.add(business.getDeliveryPrice() == null ? BigDecimal.ZERO : business.getDeliveryPrice());
    }
}
