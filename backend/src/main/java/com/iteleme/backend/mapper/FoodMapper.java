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
     * 查询某个商家的食品列表。
     */
    @Select("""
            SELECT id, name, description, image, price, business_id, remark
            FROM food
            WHERE business_id = #{businessId}
            ORDER BY id
            """)
    List<Food> findByBusinessId(@Param("businessId") Integer businessId);

    /**
     * 根据食品编号查询食品。
     */
    @Select("""
            SELECT id, name, description, image, price, business_id, remark
            FROM food
            WHERE id = #{foodId}
            """)
    Food findById(@Param("foodId") Integer foodId);

    /**
     * 根据食品编号和商家编号查询食品。
     */
    @Select("""
            SELECT id, name, description, image, price, business_id, remark
            FROM food
            WHERE id = #{foodId}
              AND business_id = #{businessId}
            """)
    Food findByIdAndBusinessId(@Param("foodId") Integer foodId,
                               @Param("businessId") Integer businessId);
}
