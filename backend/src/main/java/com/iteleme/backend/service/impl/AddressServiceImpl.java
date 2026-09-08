package com.iteleme.backend.service.impl;

import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.dto.DeliveryAddressSaveRequest;
import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.mapper.AddressMapper;
import com.iteleme.backend.service.AddressService;
import com.iteleme.backend.vo.DeliveryAddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressMapper addressMapper;

    @Override
    public List<DeliveryAddressVO> list() {
        Integer userId = CurrentUserContext.userId();
        return addressMapper.list(userId).stream().map(DeliveryAddressVO::from).toList();
    }

    @Override
    public DeliveryAddressVO get(Integer id) {
        return DeliveryAddressVO.from(loadAddress(id));
    }

    @Override
    @Transactional
    public DeliveryAddressVO create(DeliveryAddressSaveRequest request) {
        Integer userId = CurrentUserContext.userId();
        DeliveryAddress address = new DeliveryAddress(
                addressMapper.nextId(),
                userId,
                request.address(),
                request.contactName(),
                request.contactTel(),
                request.contactSex(),
                0
        );
        addressMapper.insert(address);
        return DeliveryAddressVO.from(address);
    }

    @Override
    @Transactional
    public DeliveryAddressVO update(Integer id, DeliveryAddressSaveRequest request) {
        DeliveryAddress address = loadAddress(id);
        address.setAddress(request.address());
        address.setContactName(request.contactName());
        address.setContactTel(request.contactTel());
        address.setContactSex(request.contactSex());
        address.setIsDeleted(0);
        addressMapper.update(address);
        return DeliveryAddressVO.from(address);
    }

    @Override
    @Transactional
    public void remove(Integer id) {
        DeliveryAddress address = loadAddress(id);
        addressMapper.remove(address.getId(), address.getUserId());
    }

    private DeliveryAddress loadAddress(Integer id) {
        DeliveryAddress address = addressMapper.findById(id, CurrentUserContext.userId());
        if (address == null) {
            throw new NotFoundException("收货地址不存在");
        }
        return address;
    }
}
