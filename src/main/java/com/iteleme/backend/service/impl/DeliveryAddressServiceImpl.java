package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.exception.ResourceNotFoundException;
import com.iteleme.backend.mapper.DeliveryAddressMapper;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.DeliveryAddressService;
import com.iteleme.backend.vo.request.DeliveryAddressRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryAddressServiceImpl implements DeliveryAddressService {
    private final DeliveryAddressMapper addressMapper;
    private final UserMapper userMapper;

    public DeliveryAddressServiceImpl(DeliveryAddressMapper addressMapper, UserMapper userMapper) {
        this.addressMapper = addressMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<DeliveryAddress> listByUserId(String userId) {
        requireUserId(userId);
        requireUser(userId);
        return addressMapper.findByUserId(userId);
    }

    @Override
    public DeliveryAddress getById(Integer daId) {
        requireId(daId);
        DeliveryAddress address = addressMapper.findById(daId);
        if (address == null) throw new ResourceNotFoundException("地址不存在");
        return address;
    }

    @Override
    public DeliveryAddress create(String userId, DeliveryAddressRequest request) {
        requireUserId(userId);
        requireUser(userId);
        validate(request);
        DeliveryAddress address = toEntity(request);
        address.setUserId(userId);
        addressMapper.insert(address);
        return address;
    }

    @Override
    public DeliveryAddress update(Integer daId, DeliveryAddressRequest request) {
        DeliveryAddress existing = getById(daId);
        validate(request);
        existing.setContactName(request.getContactName());
        existing.setContactSex(request.getContactSex());
        existing.setContactTel(request.getContactTel());
        existing.setAddress(request.getAddress());
        addressMapper.update(existing);
        return existing;
    }

    @Override
    public void delete(Integer daId) {
        getById(daId);
        addressMapper.deleteById(daId);
    }

    private void requireUser(String userId) {
        if (userMapper.findActiveById(userId) == null) throw new ResourceNotFoundException("用户不存在");
    }
    private static void requireUserId(String userId) {
        if (userId == null || userId.isBlank() || userId.length() > 20) throw new IllegalArgumentException("userId 无效");
    }
    private static void requireId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("daId 无效");
    }
    private static void validate(DeliveryAddressRequest request) {
        if (request == null) throw new IllegalArgumentException("请求体不能为空");
        if (request.getContactName() == null || request.getContactName().isBlank() || request.getContactName().length() > 20) throw new IllegalArgumentException("contactName 无效");
        if (request.getContactSex() == null || (request.getContactSex() != 0 && request.getContactSex() != 1)) throw new IllegalArgumentException("contactSex 无效");
        if (request.getContactTel() == null || request.getContactTel().isBlank() || request.getContactTel().length() > 20) throw new IllegalArgumentException("contactTel 无效");
        if (request.getAddress() == null || request.getAddress().isBlank() || request.getAddress().length() > 100) throw new IllegalArgumentException("address 无效");
    }
    private static DeliveryAddress toEntity(DeliveryAddressRequest request) {
        DeliveryAddress address = new DeliveryAddress();
        address.setContactName(request.getContactName());
        address.setContactSex(request.getContactSex());
        address.setContactTel(request.getContactTel());
        address.setAddress(request.getAddress());
        return address;
    }
}
