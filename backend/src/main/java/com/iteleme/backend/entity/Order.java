package com.iteleme.backend.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Order {
    private Integer orderId;
    private String userId;
    private Integer businessId;
    private String orderDate;
    private BigDecimal orderTotal;
    private Integer daId;
    private Integer orderState;
}
