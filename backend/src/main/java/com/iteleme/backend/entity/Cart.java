package com.iteleme.backend.entity;

import lombok.Data;

@Data
public class Cart {
    private Integer cartId;
    private Integer foodId;
    private Integer businessId;
    private String userId;
    private Integer quantity;
}
