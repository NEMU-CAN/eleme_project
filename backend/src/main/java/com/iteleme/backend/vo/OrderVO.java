package com.iteleme.backend.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderVO {
    private Integer orderId;
    private String userId;
    private Integer businessId;
    private String orderDate;
    private BigDecimal orderTotal;
    private Integer daId;
    private Integer orderState;
    private BusinessVO business;
    private DeliveryAddressVO deliveryAddress;
    private List<OrderItemVO> items = new ArrayList<>();
}
