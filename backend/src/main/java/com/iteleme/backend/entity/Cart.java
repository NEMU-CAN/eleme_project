package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private Integer id;
    private Integer userId;
    private Integer businessId;
    private Integer foodId;
    private Integer quantity;
}
