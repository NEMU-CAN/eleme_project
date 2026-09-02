package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物车实体，对应 `cart` 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    /** 购物车编号。 */
    private Integer id;
    /** 食品编号。 */
    private Integer foodId;
    /** 商家编号。 */
    private Integer businessId;
    /** 用户编号。 */
    private String userId;
    /** 同一食品的购买数量。 */
    private Integer quantity;
}
