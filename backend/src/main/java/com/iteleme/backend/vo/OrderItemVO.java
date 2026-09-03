package com.iteleme.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单明细展示对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemVO {
    /** 订单明细编号。 */
    private Integer id;
    /** 所属订单编号。 */
    private Integer orderId;
    /** 食品编号。 */
    private Integer foodId;
    /** 数量。 */
    private Integer quantity;
    /** 食品详情。 */
    private FoodVO food;
}
