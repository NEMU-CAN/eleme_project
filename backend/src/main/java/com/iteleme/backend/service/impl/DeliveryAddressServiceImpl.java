package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.DeliveryAddressMapper;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.DeliveryAddressService;
import com.iteleme.backend.vo.DeliveryAddressVO;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAddressServiceImpl implements DeliveryAddressService {
    private final DeliveryAddressMapper deliveryAddressMapper;
    private final UserMapper userMapper;

    @Override
    public List<DeliveryAddressVO> listDeliveryAddressesByUserId(String userId) {
        ensureActiveUser(userId);
        return deliveryAddressMapper.findByUserId(userId).stream()
                .map(VoConverters::toDeliveryAddressVO)
                .toList();
    }

    @Override
    public DeliveryAddressVO getDeliveryAddressById(String userId, Integer daId) {
        ensureActiveUser(userId);
        ServiceValidator.requirePositive(daId, "daId");
        DeliveryAddress deliveryAddress = deliveryAddressMapper.findByIdForUser(userId, daId);
        if (deliveryAddress == null) {
            throw ApiException.notFound();
        }
        return VoConverters.toDeliveryAddressVO(deliveryAddress);
    }

    @Override
    public DeliveryAddressVO createDeliveryAddress(String userId, DeliveryAddressRequest request) {
        ensureActiveUser(userId);
        validateRequest(request);

        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setUserId(userId);
        deliveryAddress.setContactName(request.getContactName());
        deliveryAddress.setContactSex(request.getContactSex());
        deliveryAddress.setContactTel(request.getContactTel());
        deliveryAddress.setAddress(request.getAddress());
        deliveryAddressMapper.insert(deliveryAddress);
        return VoConverters.toDeliveryAddressVO(deliveryAddress);
    }

    @Override
    public DeliveryAddressVO updateDeliveryAddress(String userId, Integer daId, DeliveryAddressRequest request) {
        ensureActiveUser(userId);
        ServiceValidator.requirePositive(daId, "daId");
        validateRequest(request);
        if (deliveryAddressMapper.findByIdForUser(userId, daId) == null) {
            throw ApiException.notFound();
        }

        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setDaId(daId);
        deliveryAddress.setUserId(userId);
        deliveryAddress.setContactName(request.getContactName());
        deliveryAddress.setContactSex(request.getContactSex());
        deliveryAddress.setContactTel(request.getContactTel());
        deliveryAddress.setAddress(request.getAddress());
        deliveryAddressMapper.update(deliveryAddress);
        return VoConverters.toDeliveryAddressVO(deliveryAddressMapper.findByIdForUser(userId, daId));
    }

    @Override
    public void deleteDeliveryAddress(String userId, Integer daId) {
        ensureActiveUser(userId);
        ServiceValidator.requirePositive(daId, "daId");
        if (deliveryAddressMapper.findByIdForUser(userId, daId) == null) {
            throw ApiException.notFound();
        }
        int affectedRows = deliveryAddressMapper.deleteByIdForUser(userId, daId);
        if (affectedRows == 0) {
            throw ApiException.notFound();
        }
    }

    private void validateRequest(DeliveryAddressRequest request) {
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requireNonBlank(request.getContactName(), "contactName");
        ServiceValidator.requireMaxLength(request.getContactName(), "contactName", 20);
        ServiceValidator.requireZeroOrOne(request.getContactSex(), "contactSex");
        ServiceValidator.requireNonBlank(request.getContactTel(), "contactTel");
        ServiceValidator.requireMaxLength(request.getContactTel(), "contactTel", 20);
        ServiceValidator.requireNonBlank(request.getAddress(), "address");
        ServiceValidator.requireMaxLength(request.getAddress(), "address", 100);
    }

    private void ensureActiveUser(String userId) {
        ServiceValidator.requireUserId(userId);
        if (userMapper.findActiveById(userId) == null) {
            throw ApiException.notFound();
        }
    }
}
