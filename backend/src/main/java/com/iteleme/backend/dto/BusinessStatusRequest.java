package com.iteleme.backend.dto;

import jakarta.validation.constraints.NotNull;

public record BusinessStatusRequest(
        @NotNull(message = "状态不能为空") Integer status
) {
}
