package com.iteleme.backend.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BusinessVO {
    private Integer businessId;
    private String businessName;
    private String businessAddress;
    private String businessExplain;
    private String businessImg;
    private Integer orderTypeId;
    private BigDecimal starPrice;
    private BigDecimal deliveryPrice;
    private String remarks;
}
