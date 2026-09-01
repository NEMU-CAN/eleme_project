package com.iteleme.backend.vo;

import lombok.Data;

@Data
public class CartItemVO {
    private Integer cartId;
    private String userId;
    private Integer businessId;
    private Integer foodId;
    private Integer quantity;
    private BusinessVO business;
    private FoodVO food;
}
