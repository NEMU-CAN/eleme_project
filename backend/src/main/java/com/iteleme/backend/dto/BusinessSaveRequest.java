package com.iteleme.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BusinessSaveRequest(
        @NotBlank(message = "商家名称不能为空") @Size(max = 40, message = "商家名称长度不能超过40") String name,
        @NotBlank(message = "商家地址不能为空") @Size(max = 100, message = "商家地址长度不能超过100") String address,
        @Size(max = 255, message = "商家简介长度不能超过255") String description,
        String image,
        @NotNull(message = "口味分类不能为空") Integer tasteId,
        BigDecimal startPrice,
        BigDecimal deliveryPrice,
        Integer status
) {
}
