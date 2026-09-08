package com.iteleme.backend.vo;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Orders;

public record OrderSummaryVO(
        Orders order,
        Business business,
        DeliveryAddressVO deliveryAddress,
        Integer itemCount
) {
}
