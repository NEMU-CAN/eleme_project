package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.service.BusinessService;
import com.iteleme.backend.vo.BusinessVO;
import com.iteleme.backend.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private BusinessMapper businessMapper;
    @Autowired
    private FoodMapper foodMapper;

    /**
     * 查询商家列表。
     */
    @Override
    public List<BusinessVO> listBusinesses(Integer orderTypeId) {
        ServiceValidator.requireOptionalPositive(orderTypeId, "orderTypeId");
        return businessMapper.list(orderTypeId).stream()
                .map(VoConverters::toBusinessVO)
                .toList();
    }

    /**
     * 查询商家详情。
     */
    @Override
    public BusinessVO getBusinessById(Integer businessId) {
        ServiceValidator.requirePositive(businessId, "businessId");
        Business business = businessMapper.findById(businessId);
        if (business == null) {
            throw ApiException.notFound();
        }
        return VoConverters.toBusinessVO(business);
    }

    /**
     * 查询商家食品列表。
     */
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
