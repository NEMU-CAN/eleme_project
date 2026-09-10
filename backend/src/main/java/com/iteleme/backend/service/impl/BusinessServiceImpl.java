package com.iteleme.backend.service.impl;

import com.iteleme.backend.constant.BusinessStatus;
import com.iteleme.backend.constant.FoodStatus;
import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.dto.BusinessSaveRequest;
import com.iteleme.backend.dto.BusinessStatusRequest;
import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.BusinessAdmin;
import com.iteleme.backend.entity.Taste;
import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.BadRequestException;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.mapper.BusinessAdminMapper;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.mapper.TasteMapper;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.AccessService;
import com.iteleme.backend.service.BusinessService;
import com.iteleme.backend.vo.BusinessVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {
    private final BusinessMapper businessMapper;
    private final BusinessAdminMapper businessAdminMapper;
    private final FoodMapper foodMapper;
    private final TasteMapper tasteMapper;
    private final UserMapper userMapper;
    private final AccessService accessService;

    @Override
    public List<Business> list(Integer tasteId, Integer status, String keyword) {
        return businessMapper.list(tasteId, status, keyword);
    }

    @Override
    public BusinessVO get(Integer id) {
        Business business = loadBusiness(id);
        return new BusinessVO(business, foodMapper.list(id, FoodStatus.ONLINE, null));
    }

    @Override
    @Transactional
    public BusinessVO create(BusinessSaveRequest request) {
        Integer currentUserId = CurrentUserContext.userId();
        validateTaste(request.tasteId());

        Integer businessId = businessMapper.nextId();
        Business business = new Business(
                businessId,
                request.name(),
                request.address(),
                request.description(),
                request.image(),
                request.tasteId(),
                defaultStartPrice(request.startPrice()),
                defaultDeliveryPrice(request.deliveryPrice()),
                request.status() == null ? BusinessStatus.OPEN : request.status()
        );
        businessMapper.insert(business);

        BusinessAdmin admin = new BusinessAdmin(businessAdminMapper.nextId(), currentUserId, businessId);
        businessAdminMapper.insert(admin);

        User user = userMapper.findById(currentUserId);
        if (user != null && user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.BUSINESS) {
            user.setRole(UserRole.BUSINESS);
            userMapper.update(user);
        }

        return new BusinessVO(business, List.of());
    }

    @Override
    @Transactional
    public BusinessVO update(Integer id, BusinessSaveRequest request) {
        Business business = loadBusiness(id);
        accessService.ensureBusinessOwner(id);
        validateTaste(request.tasteId() == null ? business.getTasteId() : request.tasteId());

        business.setName(request.name() != null ? request.name() : business.getName());
        business.setAddress(request.address() != null ? request.address() : business.getAddress());
        business.setDescription(request.description() != null ? request.description() : business.getDescription());
        business.setImage(request.image() != null ? request.image() : business.getImage());
        business.setTasteId(request.tasteId() != null ? request.tasteId() : business.getTasteId());
        business.setStartPrice(request.startPrice() != null ? request.startPrice() : business.getStartPrice());
        business.setDeliveryPrice(request.deliveryPrice() != null ? request.deliveryPrice() : business.getDeliveryPrice());
        business.setStatus(request.status() != null ? request.status() : business.getStatus());

        businessMapper.update(business);
        return new BusinessVO(business, foodMapper.list(id, FoodStatus.ONLINE, null));
    }

    @Override
    @Transactional
    public void status(Integer id, BusinessStatusRequest request) {
        Business business = loadBusiness(id);
        accessService.ensureBusinessOwner(id);
        if (request.status() != BusinessStatus.CLOSED
                && request.status() != BusinessStatus.OPEN
                && request.status() != BusinessStatus.DELETED) {
            throw new BadRequestException("非法商家状态");
        }
        businessMapper.updateStatus(business.getId(), request.status());
    }

    private Business loadBusiness(Integer id) {
        Business business = businessMapper.findById(id);
        if (business == null || BusinessStatus.DELETED == business.getStatus()) {
            throw new NotFoundException("商家不存在");
        }
        return business;
    }

    private void validateTaste(Integer tasteId) {
        Taste taste = tasteMapper.findById(tasteId);
        if (taste == null) {
            throw new NotFoundException("口味不存在");
        }
    }

    private BigDecimal defaultStartPrice(BigDecimal startPrice) {
        return startPrice == null ? BigDecimal.ZERO : startPrice;
    }

    private BigDecimal defaultDeliveryPrice(BigDecimal deliveryPrice) {
        return deliveryPrice == null ? BigDecimal.ZERO : deliveryPrice;
    }
}
