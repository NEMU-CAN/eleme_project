package com.iteleme.backend.service.impl;

import com.iteleme.backend.constant.BusinessStatus;
import com.iteleme.backend.constant.FoodStatus;
import com.iteleme.backend.dto.FoodSaveRequest;
import com.iteleme.backend.dto.FoodStatusRequest;
import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.exception.BadRequestException;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.service.AccessService;
import com.iteleme.backend.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {
    private final FoodMapper foodMapper;
    private final BusinessMapper businessMapper;
    private final AccessService accessService;

    @Override
    public List<Food> list(Integer businessId, Integer status, String keyword) {
        return foodMapper.list(businessId, status, keyword);
    }

    @Override
    public Food get(Integer id) {
        return loadFood(id);
    }

    @Override
    @Transactional
    public Food create(FoodSaveRequest request) {
        ensureBusinessActive(request.businessId());
        accessService.ensureBusinessOwner(request.businessId());

        Food food = new Food(
                foodMapper.nextId(),
                request.name(),
                request.description(),
                request.image(),
                request.price(),
                request.businessId(),
                request.stock() == null ? 0 : request.stock(),
                0,
                request.status() == null ? FoodStatus.ONLINE : request.status()
        );
        foodMapper.insert(food);
        return food;
    }

    @Override
    @Transactional
    public Food update(Integer id, FoodSaveRequest request) {
        Food food = loadFood(id);
        accessService.ensureBusinessOwner(food.getBusinessId());
        if (request.businessId() != null && !request.businessId().equals(food.getBusinessId())) {
            throw new BadRequestException("不允许修改商品所属商家");
        }

        food.setName(StringUtils.hasText(request.name()) ? request.name() : food.getName());
        food.setDescription(request.description() != null ? request.description() : food.getDescription());
        food.setImage(request.image() != null ? request.image() : food.getImage());
        food.setPrice(request.price() != null ? request.price() : food.getPrice());
        int nextStock = request.stock() != null ? request.stock() : food.getStock();
        if (nextStock < food.getReservedStock()) {
            throw new BadRequestException("库存不能小于已预占库存");
        }
        food.setStock(nextStock);
        food.setStatus(request.status() != null ? request.status() : food.getStatus());

        foodMapper.update(food);
        return food;
    }

    @Override
    @Transactional
    public void status(Integer id, FoodStatusRequest request) {
        Food food = loadFood(id);
        accessService.ensureBusinessOwner(food.getBusinessId());
        if (request.status() != FoodStatus.OFFLINE && request.status() != FoodStatus.ONLINE) {
            throw new BadRequestException("非法商品状态");
        }
        foodMapper.updateStatus(id, request.status());
    }

    private void ensureBusinessActive(Integer businessId) {
        Business business = businessMapper.findById(businessId);
        if (business == null || business.getStatus() == BusinessStatus.DELETED) {
            throw new NotFoundException("商家不存在");
        }
    }

    private Food loadFood(Integer id) {
        Food food = foodMapper.findById(id);
        if (food == null) {
            throw new NotFoundException("商品不存在");
        }
        return food;
    }
}
