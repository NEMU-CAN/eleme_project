package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    List<Cart> list(@Param("userId") Integer userId, @Param("businessId") Integer businessId);

    Cart findByUserAndFood(@Param("userId") Integer userId, @Param("foodId") Integer foodId);

    int insert(Cart cart);

    int updateQuantity(@Param("userId") Integer userId, @Param("foodId") Integer foodId, @Param("quantity") Integer quantity);

    int deleteByUserAndFood(@Param("userId") Integer userId, @Param("foodId") Integer foodId);

    int clearByUser(@Param("userId") Integer userId);

    int clearByUserAndBusiness(@Param("userId") Integer userId, @Param("businessId") Integer businessId);

    Integer nextId();
}
