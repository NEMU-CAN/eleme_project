package com.iteleme.backend.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Food {
    private Integer foodId;
    private String foodName;
    private String foodExplain;
    private String foodImg;
    private BigDecimal foodPrice;
    private Integer businessId;
    private String remarks;
}
