package com.iteleme.backend.service;

import com.iteleme.backend.dto.BusinessSaveRequest;
import com.iteleme.backend.dto.BusinessStatusRequest;
import com.iteleme.backend.entity.Business;
import com.iteleme.backend.vo.BusinessVO;

import java.util.List;

public interface BusinessService {
    List<Business> list(Integer tasteId, Integer status, String keyword);

    BusinessVO get(Integer id);

    BusinessVO create(BusinessSaveRequest request);

    BusinessVO update(Integer id, BusinessSaveRequest request);

    void status(Integer id, BusinessStatusRequest request);
}
