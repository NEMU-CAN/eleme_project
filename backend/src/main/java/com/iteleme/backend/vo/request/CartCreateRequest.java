package com.iteleme.backend.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCreateRequest {
    private Integer businessId;
    private Integer foodId;
    private Integer quantity;
}
