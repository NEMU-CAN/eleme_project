package com.iteleme.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物车展示对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemVO {
    /** 购物车编号。 */
    private Integer id;
    /** 用户编号。 */
    private String userId;
    /** 商家编号。 */
    private Integer businessId;
    /** 食品编号。 */
    private Integer foodId;
    /** 数量。 */
    private Integer quantity;
    /** 商家详情。 */
    private BusinessVO business;
    /** 食品详情。 */
    private FoodVO food;
}
