package com.iteleme.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer businessId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private BigDecimal deliveryPrice;
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;
    private Integer deliveryAddressId;
    private Integer orderStatus;
}
