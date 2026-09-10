package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.dto.DeliveryAddressSaveRequest;
import com.iteleme.backend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/addresses")
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public Result list() {
        return Result.success(addressService.list());
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Integer id) {
        return Result.success(addressService.get(id));
    }

    @PostMapping
    public ResponseEntity<Result> create(@RequestBody @Valid DeliveryAddressSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(addressService.create(request)));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody @Valid DeliveryAddressSaveRequest request) {
        return Result.success(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result remove(@PathVariable Integer id) {
        addressService.remove(id);
        return Result.success();
    }
}
