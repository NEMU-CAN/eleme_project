package com.iteleme.backend.controller;

import com.iteleme.backend.service.BusinessService;
import com.iteleme.backend.vo.BusinessVO;
import com.iteleme.backend.vo.FoodVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {
    private final BusinessService businessService;

    @GetMapping
    public List<BusinessVO> listBusinesses(@RequestParam(value = "orderTypeId", required = false) Integer orderTypeId) {
        return businessService.listBusinesses(orderTypeId);
    }

    @GetMapping("/{businessId}")
    public BusinessVO getBusinessById(@PathVariable("businessId") Integer businessId) {
        return businessService.getBusinessById(businessId);
    }

    @GetMapping("/{businessId}/foods")
    public List<FoodVO> listFoodsByBusinessId(@PathVariable("businessId") Integer businessId) {
        return businessService.listFoodsByBusinessId(businessId);
    }
}
