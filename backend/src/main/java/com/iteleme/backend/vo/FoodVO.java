package com.iteleme.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 食品展示对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodVO {
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
