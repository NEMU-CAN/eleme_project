package com.iteleme.backend.service;

import com.iteleme.backend.dto.DeliveryAddressSaveRequest;
import com.iteleme.backend.vo.DeliveryAddressVO;

import java.util.List;

public interface AddressService {
    List<DeliveryAddressVO> list();

    DeliveryAddressVO get(Integer id);

    DeliveryAddressVO create(DeliveryAddressSaveRequest request);

    DeliveryAddressVO update(Integer id, DeliveryAddressSaveRequest request);

    void remove(Integer id);
}
