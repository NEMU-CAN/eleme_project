package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.CartMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.CartService;
import com.iteleme.backend.vo.CartItemVO;
import com.iteleme.backend.vo.request.CartCreateRequest;
import com.iteleme.backend.vo.request.CartUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartMapper cartMapper;
    private final UserMapper userMapper;
    private final BusinessMapper businessMapper;
    private final FoodMapper foodMapper;

    @Override
    public List<CartItemVO> listCartItems(String userId, Integer businessId) {
        ensureActiveUser(userId);
        ServiceValidator.requireOptionalPositive(businessId, "businessId");
        return cartMapper.findByUserId(userId, businessId).stream()
                .map(this::assembleCartItem)
                .toList();
    }

    @Override
    public CartItemVO upsertCartItem(String userId, CartCreateRequest request) {
        ensureActiveUser(userId);
        validateCreateRequest(request);

        Business business = businessMapper.findById(request.getBusinessId());
        Food food = foodMapper.findByIdAndBusinessId(request.getFoodId(), request.getBusinessId());
        if (business == null || food == null) {
            throw ApiException.notFound();
        }

        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        Cart existing = cartMapper.findExisting(userId, request.getBusinessId(), request.getFoodId());
        if (existing != null) {
            cartMapper.increaseQuantity(existing.getCartId(), quantity);
            Cart updated = cartMapper.findByIdForUser(userId, existing.getCartId());
            return VoConverters.toCartItemVO(updated, business, food);
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setBusinessId(request.getBusinessId());
        cart.setFoodId(request.getFoodId());
        cart.setQuantity(quantity);
        cartMapper.insert(cart);
        return VoConverters.toCartItemVO(cart, business, food);
    }

    @Override
    public CartItemVO updateCartItemQuantity(String userId, Integer cartId, CartUpdateRequest request) {
        ensureActiveUser(userId);
        ServiceValidator.requirePositive(cartId, "cartId");
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requirePositive(request.getQuantity(), "quantity");

        Cart cart = cartMapper.findByIdForUser(userId, cartId);
        if (cart == null) {
            throw ApiException.notFound();
        }
        cartMapper.updateQuantity(userId, cartId, request.getQuantity());
        Cart updated = cartMapper.findByIdForUser(userId, cartId);
        return assembleCartItem(updated);
    }

    @Override
    public void deleteCartItemsByFilter(String userId, Integer businessId, Integer foodId) {
        ensureActiveUser(userId);
        ServiceValidator.requireOptionalPositive(businessId, "businessId");
        ServiceValidator.requireOptionalPositive(foodId, "foodId");
        cartMapper.deleteByFilter(userId, businessId, foodId);
    }

    @Override
    public void deleteCartItem(String userId, Integer cartId) {
        ensureActiveUser(userId);
        ServiceValidator.requirePositive(cartId, "cartId");
        if (cartMapper.findByIdForUser(userId, cartId) == null) {
            throw ApiException.notFound();
        }
        int affectedRows = cartMapper.deleteByIdForUser(userId, cartId);
        if (affectedRows == 0) {
            throw ApiException.notFound();
        }
    }

    private void validateCreateRequest(CartCreateRequest request) {
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requirePositive(request.getBusinessId(), "businessId");
        ServiceValidator.requirePositive(request.getFoodId(), "foodId");
        if (request.getQuantity() != null) {
            ServiceValidator.requirePositive(request.getQuantity(), "quantity");
        }
    }

    private CartItemVO assembleCartItem(Cart cart) {
        Business business = businessMapper.findById(cart.getBusinessId());
        Food food = foodMapper.findById(cart.getFoodId());
        return VoConverters.toCartItemVO(cart, business, food);
    }

    private void ensureActiveUser(String userId) {
        ServiceValidator.requireUserId(userId);
        if (userMapper.findActiveById(userId) == null) {
            throw ApiException.notFound();
        }
    }
}
