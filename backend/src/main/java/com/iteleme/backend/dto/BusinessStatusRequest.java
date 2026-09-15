package com.iteleme.backend.dto;

import com.iteleme.backend.constant.BusinessStatus;
import jakarta.validation.constraints.NotNull;

public record BusinessStatusRequest(
        @NotNull(message = "状态不能为空") BusinessStatus status
) {
}
