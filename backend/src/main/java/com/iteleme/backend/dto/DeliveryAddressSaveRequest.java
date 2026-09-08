package com.iteleme.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryAddressSaveRequest(
        @NotBlank(message = "地址不能为空") @Size(max = 100, message = "地址长度不能超过100") String address,
        @NotBlank(message = "联系人姓名不能为空") @Size(max = 20, message = "联系人姓名长度不能超过20") String contactName,
        @NotBlank(message = "联系人电话不能为空") @Size(max = 20, message = "联系人电话长度不能超过20") String contactTel,
        Integer contactSex
) {
}
