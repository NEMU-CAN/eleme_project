package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Food;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FoodMapper {
    List<Food> list(@Param("businessId") Integer businessId,
                    @Param("status") Integer status,
                    @Param("keyword") String keyword);

    Food findById(@Param("id") Integer id);

    int insert(Food food);

    int update(Food food);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    int deduct(@Param("id") Integer id, @Param("quantity") Integer quantity);

    Integer nextId();
}
