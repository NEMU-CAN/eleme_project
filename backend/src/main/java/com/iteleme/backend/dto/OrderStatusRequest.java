package com.iteleme.backend.dto;

import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(
        @NotNull(message = "订单状态不能为空") Integer orderStatus
) {
}
