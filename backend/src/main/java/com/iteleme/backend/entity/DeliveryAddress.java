package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    private Integer id;
    private Integer userId;
    private String address;
    private String contactName;
    private String contactTel;
    private Integer contactGender;
    private Integer isDeleted;
}
