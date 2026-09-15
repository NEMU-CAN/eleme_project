package com.iteleme.backend.dto;

import com.iteleme.backend.constant.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(
        @NotNull(message = "订单状态不能为空") OrderStatus orderStatus
) {
}
