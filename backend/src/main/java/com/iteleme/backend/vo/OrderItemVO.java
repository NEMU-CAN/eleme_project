package com.iteleme.backend.vo;

import lombok.Data;

@Data
public class OrderItemVO {
    private Integer odId;
    private Integer orderId;
    private Integer foodId;
    private Integer quantity;
    private FoodVO food;
}
