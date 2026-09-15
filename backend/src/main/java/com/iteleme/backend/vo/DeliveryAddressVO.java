package com.iteleme.backend.vo;

import com.iteleme.backend.constant.GenderType;
import com.iteleme.backend.entity.DeliveryAddress;

public record DeliveryAddressVO(
        Integer id,
        Integer userId,
        String address,
        String contactName,
        String contactTel,
        GenderType contactGender
) {
    public static DeliveryAddressVO from(DeliveryAddress address) {
        return new DeliveryAddressVO(
                address.getId(),
                address.getUserId(),
                address.getAddress(),
                address.getContactName(),
                address.getContactTel(),
                address.getContactGender()
        );
    }
}
