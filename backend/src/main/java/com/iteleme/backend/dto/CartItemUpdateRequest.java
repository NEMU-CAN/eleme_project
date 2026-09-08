package com.iteleme.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemUpdateRequest(
        @NotNull(message = "数量不能为空") @Min(value = 1, message = "数量必须大于0") Integer quantity
) {
}
