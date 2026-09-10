package com.iteleme.backend.vo;

import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.entity.OrderDetail;
import com.iteleme.backend.entity.Orders;

import java.util.List;

public record OrderDetailVO(
        Orders order,
        DeliveryAddress deliveryAddress,
        List<OrderDetail> details
) {
}
