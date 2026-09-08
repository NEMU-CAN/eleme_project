package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Business {
    private Integer id;
    private String name;
    private String address;
    private String description;
    private String image;
    private Integer tasteId;
    private BigDecimal startPrice;
    private BigDecimal deliveryPrice;
    private Integer status;
}
