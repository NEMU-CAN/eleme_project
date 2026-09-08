package com.iteleme.backend.dto;

import jakarta.validation.constraints.NotNull;

public record FoodStatusRequest(
        @NotNull(message = "状态不能为空") Integer status
) {
}
