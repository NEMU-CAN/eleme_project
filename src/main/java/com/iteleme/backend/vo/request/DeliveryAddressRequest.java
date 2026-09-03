package com.iteleme.backend.vo.request;

import lombok.Data;

@Data
public class DeliveryAddressRequest {
    private String contactName;
    private Integer contactSex;
    private String contactTel;
    private String address;
}
