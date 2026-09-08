package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.dto.FoodSaveRequest;
import com.iteleme.backend.dto.FoodStatusRequest;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.service.FoodService;
import com.iteleme.backend.support.RequestValues;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/foods")
public class FoodController {
    private final FoodService foodService;

    @GetMapping
    public Result list(@RequestParam(required = false) Integer businessId,
                       @RequestParam(name = "business_id", required = false) Integer businessIdSnake,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String keyword) {
        Integer resolvedBusinessId = RequestValues.first(businessId, businessIdSnake);
        List<Food> foods = foodService.list(resolvedBusinessId, status, keyword);
        return Result.success(foods);
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Integer id) {
        return Result.success(foodService.get(id));
    }

    @PostMapping
    public ResponseEntity<Result> create(@RequestBody @Valid FoodSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(foodService.create(request)));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody @Valid FoodSaveRequest request) {
        return Result.success(foodService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public Result status(@PathVariable Integer id, @RequestBody @Valid FoodStatusRequest request) {
        foodService.status(id, request);
        return Result.success();
    }
}
