package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单明细实体，对应 `orderdetailet` 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    /** 订单明细编号。 */
    private Integer id;
    /** 所属订单编号。 */
    private Integer orderId;
    /** 食品编号。 */
    private Integer foodId;
    /** 数量。 */
    private Integer quantity;
}
