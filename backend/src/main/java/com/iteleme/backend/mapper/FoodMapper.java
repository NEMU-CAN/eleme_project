package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FoodMapper {
    @Select("""
            SELECT id, name, description, image, price,
                   business_id AS businessId, remark
            FROM food
            WHERE business_id = #{businessId}
            ORDER BY id
            """)
    List<Food> findByBusinessId(@Param("businessId") Integer businessId);

    @Select("""
            SELECT id, name, description, image, price,
                   business_id AS businessId, remark
            FROM food
            WHERE id = #{foodId}
            """)
    Food findById(@Param("foodId") Integer foodId);

    @Select("""
            SELECT id, name, description, image, price,
                   business_id AS businessId, remark
            FROM food
            WHERE id = #{foodId} AND business_id = #{businessId}
            """)
    Food findByIdAndBusinessId(@Param("foodId") Integer foodId,
                               @Param("businessId") Integer businessId);
}
