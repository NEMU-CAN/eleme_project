package com.iteleme.backend.service.support;

import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.Order;
import com.iteleme.backend.entity.OrderDetail;
import com.iteleme.backend.mapper.CartMapper;
import com.iteleme.backend.mapper.OrderDetailMapper;
import com.iteleme.backend.mapper.OrderMapper;
import com.iteleme.backend.vo.request.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ============================================================
// [重构] 订单写入器：建订单 + 插明细 + 删购物车（下单写库部分）
// ============================================================
@Component
@RequiredArgsConstructor
public class OrderWriter {
    /** 订单日期格式。 */
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 订单表数据访问对象。 */
    private final OrderMapper orderMapper;
    /** 订单明细表数据访问对象。 */
    private final OrderDetailMapper orderDetailMapper;
    /** 购物车表数据访问对象。 */
    private final CartMapper cartMapper;

    /**
     * 写入订单：建订单（状态 0=未支付）、写入收货地址快照、逐条插入订单明细、清空该商家购物车。
     *
     * @return 已写入的订单（含自增 id）
     */
    public Order write(String userId, OrderCreateRequest request, BigDecimal orderTotal, OrderContext ctx) {
        Order order = new Order();
        order.setUserId(userId);
        order.setBusinessId(request.getBusinessId());
        order.setOrderDate(LocalDateTime.now().format(ORDER_DATE_FORMATTER));
        order.setOrderTotal(orderTotal);
        order.setAddressId(request.getDaId());
        // ===== [重构] 收货地址快照：下单时复制一份地址，订单展示不依赖地址行 =====
        order.setAddressContactName(ctx.address().getContactName());
        order.setAddressContactSex(ctx.address().getContactSex());
        order.setAddressContactTel(ctx.address().getContactTel());
        order.setAddressDetail(ctx.address().getAddress());
        // ===== [重构结束] =====
        order.setOrderStatus(0);
        orderMapper.insert(order);

        for (Cart cartItem : ctx.cartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getId());
            orderDetail.setFoodId(cartItem.getFoodId());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetailMapper.insert(orderDetail);
        }
        cartMapper.deleteByFilter(userId, request.getBusinessId(), null);
        return order;
    }
}
