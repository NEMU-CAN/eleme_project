package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 食品实体，对应 `food` 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Food {
    /** 食品编号。 */
    private Integer id;
    /** 食品名称。 */
    private String name;
    /** 食品介绍。 */
    private String description;
    /** 食品图片。 */
    private String image;
    /** 食品价格。 */
    private BigDecimal price;
    /** 所属商家编号。 */
    private Integer businessId;
    /** 备注信息。 */
    private String remark;
}
