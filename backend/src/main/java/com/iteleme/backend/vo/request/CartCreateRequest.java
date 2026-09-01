package com.iteleme.backend.vo.request;

import lombok.Data;

@Data
public class CartCreateRequest {
    private Integer businessId;
    private Integer foodId;
    private Integer quantity;
}
