package com.iteleme.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotNull(message = "商家ID不能为空") Integer businessId,
        @NotNull(message = "收货地址ID不能为空")
        @JsonAlias({"daId"})
        Integer deliveryAddressId
) {
}
