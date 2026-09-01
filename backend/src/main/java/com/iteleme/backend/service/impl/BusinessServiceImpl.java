package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.service.BusinessService;
import com.iteleme.backend.vo.BusinessVO;
import com.iteleme.backend.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {
    private final BusinessMapper businessMapper;
    private final FoodMapper foodMapper;

    @Override
    public List<BusinessVO> listBusinesses(Integer orderTypeId) {
        ServiceValidator.requireOptionalPositive(orderTypeId, "orderTypeId");
        return businessMapper.list(orderTypeId).stream()
                .map(VoConverters::toBusinessVO)
                .toList();
    }

    @Override
    public BusinessVO getBusinessById(Integer businessId) {
        ServiceValidator.requirePositive(businessId, "businessId");
        Business business = businessMapper.findById(businessId);
        if (business == null) {
            throw ApiException.notFound();
        }
        return VoConverters.toBusinessVO(business);
    }

    @Override
    public List<FoodVO> listFoodsByBusinessId(Integer businessId) {
        ServiceValidator.requirePositive(businessId, "businessId");
        Business business = businessMapper.findById(businessId);
        if (business == null) {
            throw ApiException.notFound();
        }
        return foodMapper.findByBusinessId(businessId).stream()
                .map(VoConverters::toFoodVO)
                .toList();
    }
}
