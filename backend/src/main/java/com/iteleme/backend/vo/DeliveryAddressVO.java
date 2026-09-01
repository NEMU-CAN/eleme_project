package com.iteleme.backend.vo;

import lombok.Data;

@Data
public class DeliveryAddressVO {
    private Integer daId;
    private String contactName;
    private Integer contactSex;
    private String contactTel;
    private String address;
    private String userId;
}
