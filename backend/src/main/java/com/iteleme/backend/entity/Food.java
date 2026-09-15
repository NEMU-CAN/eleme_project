package com.iteleme.backend.entity;

import com.iteleme.backend.constant.FoodStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {
    private Integer id;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Integer businessId;
    private Integer stock;
    private Integer reservedStock;
    private FoodStatus status;
}
