package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FoodMapper {
    @Select("""
            SELECT foodId, foodName, foodExplain, foodImg, foodPrice, businessId, remarks
            FROM food
            WHERE businessId = #{businessId}
            ORDER BY foodId
            """)
    List<Food> findByBusinessId(@Param("businessId") Integer businessId);

    @Select("""
            SELECT foodId, foodName, foodExplain, foodImg, foodPrice, businessId, remarks
            FROM food
            WHERE foodId = #{foodId}
            """)
    Food findById(@Param("foodId") Integer foodId);

    @Select("""
            SELECT foodId, foodName, foodExplain, foodImg, foodPrice, businessId, remarks
            FROM food
            WHERE foodId = #{foodId}
              AND businessId = #{businessId}
            """)
    Food findByIdAndBusinessId(@Param("foodId") Integer foodId,
                               @Param("businessId") Integer businessId);
}
