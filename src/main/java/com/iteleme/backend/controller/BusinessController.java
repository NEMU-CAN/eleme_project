package com.iteleme.backend.controller;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {
    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public ResponseEntity<List<Business>> listBusinesses(
            @RequestParam(required = false) Integer orderTypeId) {
        return ResponseEntity.ok(businessService.listBusinesses(orderTypeId));
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<Business> getBusinessById(@PathVariable Integer businessId) {
        return ResponseEntity.ok(businessService.getBusinessById(businessId));
    }

    @GetMapping("/{businessId}/foods")
    public ResponseEntity<List<Food>> listFoodsByBusinessId(
            @PathVariable Integer businessId) {
        return ResponseEntity.ok(businessService.listFoodsByBusinessId(businessId));
    }
}