package com.iteleme.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FoodSaveRequest(
        @NotBlank(message = "菜品名称不能为空") @Size(max = 30, message = "菜品名称长度不能超过30") String name,
        @Size(max = 255, message = "菜品简介长度不能超过255") String description,
        String image,
        @NotNull(message = "菜品价格不能为空") BigDecimal price,
        @NotNull(message = "商家ID不能为空") Integer businessId,
        @jakarta.validation.constraints.Min(value = 0, message = "库存不能小于0") Integer stock,
        Integer status
) {
}
