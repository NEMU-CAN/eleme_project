package com.iteleme.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemSaveRequest(
        @NotNull(message = "商家ID不能为空") Integer businessId,
        @NotNull(message = "菜品ID不能为空") Integer foodId,
        @Min(value = 1, message = "数量必须大于0") Integer quantity
) {
}
