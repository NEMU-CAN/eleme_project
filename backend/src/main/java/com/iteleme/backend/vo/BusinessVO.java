package com.iteleme.backend.vo;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Food;

import java.util.List;

public record BusinessVO(
        Business business,
        List<Food> foods
) {
}
