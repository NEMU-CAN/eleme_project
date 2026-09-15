package com.iteleme.backend.service;

import com.iteleme.backend.constant.FoodStatus;
import com.iteleme.backend.dto.FoodSaveRequest;
import com.iteleme.backend.dto.FoodStatusRequest;
import com.iteleme.backend.entity.Food;

import java.util.List;

public interface FoodService {
    List<Food> list(Integer businessId, FoodStatus status, String keyword);

    Food get(Integer id);

    Food create(FoodSaveRequest request);

    Food update(Integer id, FoodSaveRequest request);

    void status(Integer id, FoodStatusRequest request);
}
