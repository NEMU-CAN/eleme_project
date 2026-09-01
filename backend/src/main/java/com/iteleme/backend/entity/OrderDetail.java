package com.iteleme.backend.entity;

import lombok.Data;

@Data
public class OrderDetail {
    private Integer odId;
    private Integer orderId;
    private Integer foodId;
    private Integer quantity;
}
