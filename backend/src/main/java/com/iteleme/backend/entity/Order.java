package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单实体，对应 `orders` 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    /** 订单编号。 */
    private Integer id;
    /** 用户编号。 */
    private String userId;
    /** 商家编号。 */
    private Integer businessId;
    /** 订购日期。 */
    private String orderDate;
    /** 订单总价。 */
    private BigDecimal orderTotal;
    /** 送货地址编号。 */
    private Integer addressId;
    /** 订单状态。 */
    private Integer orderStatus;
}
