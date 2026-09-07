package com.iteleme.backend.service.support;

import com.iteleme.backend.common.VoConverters;
import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.entity.Order;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.DeliveryAddressMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.mapper.OrderDetailMapper;
import com.iteleme.backend.vo.DeliveryAddressVO;
import com.iteleme.backend.vo.OrderItemVO;
import com.iteleme.backend.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// ============================================================
// [重构] 订单 VO 组装器：把 Order 实体组装成给前端展示的 OrderVO
// ============================================================
@Component
@RequiredArgsConstructor
public class OrderAssembler {
    /** 商家表数据访问对象。 */
    private final BusinessMapper businessMapper;
    /** 送货地址表数据访问对象（仅旧数据无快照时回退查询）。 */
    private final DeliveryAddressMapper deliveryAddressMapper;
    /** 订单明细表数据访问对象。 */
    private final OrderDetailMapper orderDetailMapper;
    /** 食品表数据访问对象。 */
    private final FoodMapper foodMapper;

    /**
     * 组装订单展示对象。
     */
    public OrderVO assemble(Order order) {
        Business business = businessMapper.findById(order.getBusinessId());
        // ===== [重构] 收货地址：优先用订单快照；旧数据无快照时回退查地址行 =====
        DeliveryAddressVO deliveryAddress = order.getAddressContactName() != null
                ? VoConverters.toDeliveryAddressVO(order)
                : VoConverters.toDeliveryAddressVO(deliveryAddressMapper.findByIdForUser(order.getUserId(), order.getAddressId()));
        // ===== [重构结束] =====
        OrderVO vo = VoConverters.toOrderVO(order, business, deliveryAddress);
        List<OrderItemVO> items = orderDetailMapper.findByOrderId(order.getId()).stream()
                .map(orderDetail -> VoConverters.toOrderItemVO(orderDetail, foodMapper.findById(orderDetail.getFoodId())))
                .toList();
        vo.setItems(items);
        return vo;
    }
}
