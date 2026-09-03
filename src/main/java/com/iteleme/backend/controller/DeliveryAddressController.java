package com.iteleme.backend.controller;

import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.service.DeliveryAddressService;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-addresses")
public class DeliveryAddressController {
    private final DeliveryAddressService addressService;

    public DeliveryAddressController(DeliveryAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryAddress>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(addressService.listByUserId(resolveUserId(userId)));
    }

    @PostMapping
    public ResponseEntity<DeliveryAddress> create(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.create(resolveUserId(userId), request));
    }

    @GetMapping("/{daId}")
    public ResponseEntity<DeliveryAddress> get(@PathVariable Integer daId) {
        return ResponseEntity.ok(addressService.getById(daId));
    }

    @PutMapping("/{daId}")
    public ResponseEntity<DeliveryAddress> update(@PathVariable Integer daId,
                                                   @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity.ok(addressService.update(daId, request));
    }

    @DeleteMapping("/{daId}")
    public ResponseEntity<Void> delete(@PathVariable Integer daId) {
        addressService.delete(daId);
        return ResponseEntity.noContent().build();
    }

    private static String resolveUserId(String userId) {
        if (userId == null || userId.isBlank()) return "11111111111";
        return userId;
    }
}
