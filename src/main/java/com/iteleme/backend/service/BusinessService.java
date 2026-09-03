package com.iteleme.backend.service;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Food;

import java.util.List;

/**
 * 商家相关业务服务。
 */
public interface BusinessService {
    List<Business> listBusinesses(Integer orderTypeId);

    Business getBusinessById(Integer businessId);

    List<Food> listFoodsByBusinessId(Integer businessId);
}
