package com.iteleme.backend.controller;

import com.iteleme.backend.service.DeliveryAddressService;
import com.iteleme.backend.vo.DeliveryAddressVO;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/delivery-addresses")
@RequiredArgsConstructor
public class DeliveryAddressController {
    private final DeliveryAddressService deliveryAddressService;

    @GetMapping
    public List<DeliveryAddressVO> listDeliveryAddressesByUserId(@PathVariable("userId") String userId) {
        return deliveryAddressService.listDeliveryAddressesByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<DeliveryAddressVO> createDeliveryAddress(@PathVariable("userId") String userId,
                                                                   @RequestBody DeliveryAddressRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deliveryAddressService.createDeliveryAddress(userId, request));
    }

    @GetMapping("/{daId}")
    public DeliveryAddressVO getDeliveryAddressById(@PathVariable("userId") String userId,
                                                    @PathVariable("daId") Integer daId) {
        return deliveryAddressService.getDeliveryAddressById(userId, daId);
    }

    @PutMapping("/{daId}")
    public DeliveryAddressVO updateDeliveryAddress(@PathVariable("userId") String userId,
                                                   @PathVariable("daId") Integer daId,
                                                   @RequestBody DeliveryAddressRequest request) {
        return deliveryAddressService.updateDeliveryAddress(userId, daId, request);
    }

    @DeleteMapping("/{daId}")
    public ResponseEntity<Void> deleteDeliveryAddress(@PathVariable("userId") String userId,
                                                      @PathVariable("daId") Integer daId) {
        deliveryAddressService.deleteDeliveryAddress(userId, daId);
        return ResponseEntity.noContent().build();
    }
}
