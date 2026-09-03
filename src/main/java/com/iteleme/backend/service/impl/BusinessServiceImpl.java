package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.exception.ResourceNotFoundException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.service.BusinessService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商家业务服务实现，负责商家及其食品的查询。
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    private static void requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " 必须大于 0");
        }
    }

    private static void requireOptionalPositive(Integer value, String field) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(field + " 必须大于 0");
        }
    }
    private final BusinessMapper businessMapper;
    private final FoodMapper foodMapper;

    public BusinessServiceImpl(BusinessMapper businessMapper, FoodMapper foodMapper) {
        this.businessMapper = businessMapper;
        this.foodMapper = foodMapper;
    }

    @Override
    public List<Business> listBusinesses(Integer orderTypeId) {
        requireOptionalPositive(orderTypeId, "orderTypeId");
        return businessMapper.findByOrderTypeId(orderTypeId);
    }

    @Override
    public Business getBusinessById(Integer businessId) {
        requirePositive(businessId, "businessId");
        Business business = businessMapper.findById(businessId);
        if (business == null) {
            throw new ResourceNotFoundException("商家不存在");
        }
        return business;
    }

    @Override
    public List<Food> listFoodsByBusinessId(Integer businessId) {
        requirePositive(businessId, "businessId");
        if (businessMapper.findById(businessId) == null) {
            throw new ResourceNotFoundException("商家不存在");
        }
        return foodMapper.findByBusinessId(businessId);
    }
}
