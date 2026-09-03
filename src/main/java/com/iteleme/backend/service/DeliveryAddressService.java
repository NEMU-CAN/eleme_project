package com.iteleme.backend.service;

import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;

import java.util.List;

public interface DeliveryAddressService {
    List<DeliveryAddress> listByUserId(String userId);
    DeliveryAddress getById(Integer daId);
    DeliveryAddress create(String userId, DeliveryAddressRequest request);
    DeliveryAddress update(Integer daId, DeliveryAddressRequest request);
    void delete(Integer daId);
}
