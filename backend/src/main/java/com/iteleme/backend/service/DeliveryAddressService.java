package com.iteleme.backend.service;

import com.iteleme.backend.vo.DeliveryAddressVO;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;

import java.util.List;

public interface DeliveryAddressService {
    List<DeliveryAddressVO> listDeliveryAddressesByUserId(String userId);

    DeliveryAddressVO getDeliveryAddressById(String userId, Integer daId);

    DeliveryAddressVO createDeliveryAddress(String userId, DeliveryAddressRequest request);

    DeliveryAddressVO updateDeliveryAddress(String userId, Integer daId, DeliveryAddressRequest request);

    void deleteDeliveryAddress(String userId, Integer daId);
}
