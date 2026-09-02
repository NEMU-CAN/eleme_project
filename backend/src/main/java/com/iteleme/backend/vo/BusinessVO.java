package com.iteleme.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商家展示对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessVO {
    /** 商家编号。 */
    private Integer id;
    /** 商家名称。 */
    private String name;
    /** 商家地址。 */
    private String address;
    /** 商家介绍。 */
    private String description;
    /** 商家图片（base64）。 */
    private String image;
    /** 点餐分类编号。 */
    private Integer orderTypeId;
    /** 起送费。 */
    private BigDecimal startPrice;
    /** 配送费。 */
    private BigDecimal deliveryPrice;
    /** 备注信息。 */
    private String remark;
}
