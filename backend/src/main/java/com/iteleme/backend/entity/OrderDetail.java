package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    private Integer id;
    private Integer orderId;
    private Integer foodId;
    private Integer quantity;
    private String foodName;
    private BigDecimal foodPrice;
    private BigDecimal subtotal;
}
