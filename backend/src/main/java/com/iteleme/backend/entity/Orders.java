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
    private String userNickname;
    private String userPhone;
    private String businessName;
    private String businessAddress;
    private String receiverName;
    private String receiverTel;
    private Integer receiverGender;
    private String receiverAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    private BigDecimal deliveryPrice;
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;
    private Integer deliveryAddressId;
    private Integer orderStatus;
}
