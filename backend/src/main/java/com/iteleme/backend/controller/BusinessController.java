package com.iteleme.backend.controller;

import com.iteleme.backend.entity.Result;
import com.iteleme.backend.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    /**
     * 查询商家列表
     */
    @GetMapping
    public Result listBusinesses(@RequestParam(required = false) Integer orderTypeId) {
        return Result.success(businessService.listBusinesses(orderTypeId));
    }

    /**
     * 查询商家详情。
     */
    @GetMapping("/{businessId}")
    public Result getBusinessById(@PathVariable("businessId") Integer businessId) {
        return Result.success(businessService.getBusinessById(businessId));
    }

    /**
     * 查询商家食品列表。
     */
    @GetMapping("/{businessId}/foods")
    public Result listFoodsByBusinessId(@PathVariable("businessId") Integer businessId) {
        return Result.success(businessService.listFoodsByBusinessId(businessId));
    }
}
