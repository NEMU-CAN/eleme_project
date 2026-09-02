package com.iteleme.backend.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAddressRequest {
    private String contactName;
    private Integer contactSex;
    private String contactTel;
    private String address;
}
