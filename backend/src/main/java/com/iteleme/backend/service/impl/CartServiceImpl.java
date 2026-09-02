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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/**
 * 购物车业务实现。
 */
public class CartServiceImpl implements CartService {
    /** 购物车表数据访问对象。 */
    @Autowired
    private CartMapper cartMapper;
    /** 用户表数据访问对象。 */
    @Autowired
    private UserMapper userMapper;
    /** 商家表数据访问对象。 */
    @Autowired
    private BusinessMapper businessMapper;
    /** 食品表数据访问对象。 */
    @Autowired
    private FoodMapper foodMapper;

    /**
     * 查询用户购物车列表。
     */
    @Override
    public List<CartItemVO> listCartItems(String userId, Integer businessId) {
        ensureActiveUser(userId);
        ServiceValidator.requireOptionalPositive(businessId, "businessId");
        return cartMapper.findByUserId(userId, businessId).stream()
                .map(this::assembleCartItem)
                .toList();
    }

    /**
     * 新增购物车条目；已存在相同食品时累加数量。
     */
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
            cartMapper.increaseQuantity(existing.getId(), quantity);
            Cart updated = cartMapper.findByIdForUser(userId, existing.getId());
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

    /**
     * 修改用户购物车中指定条目的数量。
     */
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

    /**
     * 按条件删除购物车条目。
     */
    @Override
    public void deleteCartItemsByFilter(String userId, Integer businessId, Integer foodId) {
        ensureActiveUser(userId);
        ServiceValidator.requireOptionalPositive(businessId, "businessId");
        ServiceValidator.requireOptionalPositive(foodId, "foodId");
        cartMapper.deleteByFilter(userId, businessId, foodId);
    }

    /**
     * 删除指定购物车条目。
     */
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

    /**
     * 校验新增购物车条目的请求体。
     */
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

    /**
     * 组装购物车条目的商家和食品展示信息。
     */
    private CartItemVO assembleCartItem(Cart cart) {
        Business business = businessMapper.findById(cart.getBusinessId());
        Food food = foodMapper.findById(cart.getFoodId());
        return VoConverters.toCartItemVO(cart, business, food);
    }

    /**
     * 确认用户存在且处于正常状态。
     */
    private void ensureActiveUser(String userId) {
        ServiceValidator.requireUserId(userId);
        if (userMapper.findActiveById(userId) == null) {
            throw ApiException.notFound();
        }
    }
}
