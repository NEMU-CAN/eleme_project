package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 食品数据访问接口。
 */
@Mapper
public interface FoodMapper {

    /**
     * 查询某个商家的食品列表。无记录时由 MyBatis 返回空列表。
     */
    @Select("""
            SELECT foodId, foodName, foodExplain, foodImg, foodPrice, businessId, remarks
            FROM food
            WHERE businessId = #{businessId}
            ORDER BY foodId
            """)
    List<Food> findByBusinessId(@Param("businessId") Integer businessId);
}
