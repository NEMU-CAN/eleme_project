package com.iteleme.backend.service;

import com.iteleme.backend.vo.BusinessVO;
import com.iteleme.backend.vo.FoodVO;

import java.util.List;

public interface BusinessService {
    List<BusinessVO> listBusinesses(Integer orderTypeId);

    BusinessVO getBusinessById(Integer businessId);

    List<FoodVO> listFoodsByBusinessId(Integer businessId);
}
