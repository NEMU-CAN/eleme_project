package com.iteleme.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单展示对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
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
    /** 商家详情。 */
    private BusinessVO business;
    /** 送货地址详情。 */
    private DeliveryAddressVO deliveryAddress;
    /** 订单明细列表。 */
    private List<OrderItemVO> items = new ArrayList<>();
}
