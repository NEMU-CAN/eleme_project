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
    // ===== [重构] 下单时收货地址快照（订单不再依赖地址行，删除/修改地址不影响历史订单） =====
    /** 下单时收货人姓名快照。 */
    private String addressContactName;
    /** 下单时收货人性别快照。 */
    private Integer addressContactSex;
    /** 下单时收货人电话快照。 */
    private String addressContactTel;
    /** 下单时收货地址快照。 */
    private String addressDetail;
    // ===== [重构结束] =====
    /** 订单状态。 */
    private Integer orderStatus;
}
