package com.iteleme.backend.entity;

import lombok.Data;

@Data
public class Food {
    private Integer foodId;
    private String foodName;
    private String foodExplain;
    private String foodImg;
    private Integer foodPrice;
    private Integer businessId;
    private String remarks;
}
