package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.dto.BusinessSaveRequest;
import com.iteleme.backend.dto.BusinessStatusRequest;
import com.iteleme.backend.entity.Business;
import com.iteleme.backend.service.BusinessService;
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
@RequestMapping("/businesses")
public class BusinessController {
    private final BusinessService businessService;

    @GetMapping
    public Result list(@RequestParam(required = false) Integer tasteId,
                       @RequestParam(name = "taste_id", required = false) Integer tasteIdSnake,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String keyword) {
        Integer resolvedTasteId = RequestValues.first(tasteId, tasteIdSnake);
        List<Business> businesses = businessService.list(resolvedTasteId, status, keyword);
        return Result.success(businesses);
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Integer id) {
        return Result.success(businessService.get(id));
    }

    @PostMapping
    public ResponseEntity<Result> create(@RequestBody @Valid BusinessSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(businessService.create(request)));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody @Valid BusinessSaveRequest request) {
        return Result.success(businessService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public Result status(@PathVariable Integer id, @RequestBody @Valid BusinessStatusRequest request) {
        businessService.status(id, request);
        return Result.success();
    }
}
