package com.iteleme.backend.controller;

import com.iteleme.backend.service.DeliveryAddressService;
import com.iteleme.backend.vo.DeliveryAddressVO;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 当前用户的收货地址 RESTful 接口。 */
@RestController
@RequestMapping("/api/delivery-addresses")
public class DeliveryAddressController {
    private static final String DEFAULT_USER_ID = "u10001";
    private final DeliveryAddressService deliveryAddressService;

    public DeliveryAddressController(DeliveryAddressService deliveryAddressService) {
        this.deliveryAddressService = deliveryAddressService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(deliveryAddressService.listDeliveryAddressesByUserId(resolveUserId(userId))
                .stream().map(DeliveryAddressController::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody DeliveryAddressRequest request) {
        DeliveryAddressVO created = deliveryAddressService.createDeliveryAddress(resolveUserId(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{daId}")
    public ResponseEntity<Map<String, Object>> get(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable Integer daId) {
        return ResponseEntity.ok(toResponse(
                deliveryAddressService.getDeliveryAddressById(resolveUserId(userId), daId)));
    }

    @PutMapping("/{daId}")
    public ResponseEntity<Map<String, Object>> update(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable Integer daId,
            @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity.ok(toResponse(
                deliveryAddressService.updateDeliveryAddress(resolveUserId(userId), daId, request)));
    }

    @DeleteMapping("/{daId}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable Integer daId) {
        deliveryAddressService.deleteDeliveryAddress(resolveUserId(userId), daId);
        return ResponseEntity.noContent().build();
    }

    private static String resolveUserId(String userId) {
        return userId == null || userId.isBlank() ? DEFAULT_USER_ID : userId;
    }

    private static Map<String, Object> toResponse(DeliveryAddressVO address) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("daId", address.getId());
        response.put("contactName", address.getContactName());
        response.put("contactSex", address.getContactSex());
        response.put("contactTel", address.getContactTel());
        response.put("address", address.getAddress());
        response.put("userId", address.getUserId());
        return response;
    }
}
