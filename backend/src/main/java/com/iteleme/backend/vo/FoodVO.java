package com.iteleme.backend.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodVO {
    private Integer foodId;
    private String foodName;
    private String foodExplain;
    private String foodImg;
    private BigDecimal foodPrice;
    private Integer businessId;
    private String remarks;
}
