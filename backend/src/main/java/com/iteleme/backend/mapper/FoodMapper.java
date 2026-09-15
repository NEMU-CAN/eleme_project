package com.iteleme.backend.mapper;

import com.iteleme.backend.constant.FoodStatus;
import com.iteleme.backend.entity.Food;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FoodMapper {
    List<Food> list(@Param("businessId") Integer businessId,
                    @Param("status") FoodStatus status,
                    @Param("keyword") String keyword);

    Food findById(@Param("id") Integer id);

    int insert(Food food);

    int update(Food food);

    int updateStatus(@Param("id") Integer id, @Param("status") FoodStatus status);

    int deduct(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int reserveStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int releaseReservedStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int consumeReservedStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    Integer nextId();
}
