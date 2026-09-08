package com.iteleme.backend.service.impl;

import com.iteleme.backend.constant.BusinessStatus;
import com.iteleme.backend.constant.FoodStatus;
import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.dto.CartItemSaveRequest;
import com.iteleme.backend.dto.CartItemUpdateRequest;
import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.exception.BadRequestException;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.CartMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.service.CartService;
import com.iteleme.backend.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartMapper cartMapper;
    private final BusinessMapper businessMapper;
    private final FoodMapper foodMapper;

    @Override
    public List<CartItemVO> list(Integer businessId) {
        ensureCustomer();
        Integer userId = CurrentUserContext.userId();
        List<Cart> carts = cartMapper.list(userId, businessId);
        List<CartItemVO> result = new ArrayList<>(carts.size());
        for (Cart cart : carts) {
            result.add(toVO(cart));
        }
        return result;
    }

    @Override
    @Transactional
    public CartItemVO add(CartItemSaveRequest request) {
        ensureCustomer();
        int quantity = request.quantity() == null ? 1 : request.quantity();
        if (quantity < 1) {
            throw new BadRequestException("数量必须大于0");
        }

        Business business = loadBusiness(request.businessId());
        if (business.getStatus() == BusinessStatus.CLOSED) {
            throw new ForbiddenException("商家已打烊");
        }
        Food food = loadFood(request.foodId());
        if (!food.getBusinessId().equals(business.getId())) {
            throw new BadRequestException("商品不属于该商家");
        }
        if (food.getStatus() != FoodStatus.ONLINE) {
            throw new ForbiddenException("商品已下架");
        }

        Integer userId = CurrentUserContext.userId();
        Cart exist = cartMapper.findByUserAndFood(userId, request.foodId());
        Cart cart;
        if (exist == null) {
            cart = new Cart(cartMapper.nextId(), userId, request.businessId(), request.foodId(), quantity);
            cartMapper.insert(cart);
        } else {
            int newQuantity = exist.getQuantity() + quantity;
            cartMapper.updateQuantity(userId, request.foodId(), newQuantity);
            exist.setQuantity(newQuantity);
            cart = exist;
        }
        return toVO(cart);
    }

    @Override
    @Transactional
    public CartItemVO update(Integer foodId, CartItemUpdateRequest request) {
        ensureCustomer();
        Cart cart = loadCart(foodId);
        cartMapper.updateQuantity(cart.getUserId(), cart.getFoodId(), request.quantity());
        cart.setQuantity(request.quantity());
        return toVO(cart);
    }

    @Override
    @Transactional
    public void remove(Integer foodId) {
        ensureCustomer();
        Cart cart = loadCart(foodId);
        cartMapper.deleteByUserAndFood(cart.getUserId(), cart.getFoodId());
    }

    @Override
    @Transactional
    public void clear(Integer businessId) {
        ensureCustomer();
        Integer userId = CurrentUserContext.userId();
        if (businessId == null) {
            cartMapper.clearByUser(userId);
            return;
        }
        cartMapper.clearByUserAndBusiness(userId, businessId);
    }

    private void ensureCustomer() {
        if (CurrentUserContext.role() != UserRole.CUSTOMER) {
            throw new ForbiddenException("无权限操作");
        }
    }

    private Cart loadCart(Integer foodId) {
        Cart cart = cartMapper.findByUserAndFood(CurrentUserContext.userId(), foodId);
        if (cart == null) {
            throw new NotFoundException("购物车条目不存在");
        }
        return cart;
    }

    private Business loadBusiness(Integer businessId) {
        Business business = businessMapper.findById(businessId);
        if (business == null || business.getStatus() == BusinessStatus.DELETED) {
            throw new NotFoundException("商家不存在");
        }
        return business;
    }

    private Food loadFood(Integer foodId) {
        Food food = foodMapper.findById(foodId);
        if (food == null) {
            throw new NotFoundException("商品不存在");
        }
        return food;
    }

    private CartItemVO toVO(Cart cart) {
        Business business = loadBusiness(cart.getBusinessId());
        Food food = loadFood(cart.getFoodId());
        return new CartItemVO(cart, business, food);
    }
}
