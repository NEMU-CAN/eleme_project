package com.iteleme.backend.dto;

import com.iteleme.backend.constant.FoodStatus;
import jakarta.validation.constraints.NotNull;

public record FoodStatusRequest(
        @NotNull(message = "状态不能为空") FoodStatus status
) {
}
