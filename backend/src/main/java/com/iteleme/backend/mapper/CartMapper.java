package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Cart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 购物车数据访问接口。
 */
@Mapper
public interface CartMapper {
    /**
     * 查询某个用户的购物车条目，可按商家筛选。
     *
     * @param userId 用户编号
     * @param businessId 商家编号
     * @return 购物车列表
     */
    List<Cart> findByUserId(@Param("userId") String userId, @Param("businessId") Integer businessId);

    /**
     * 查询指定用户某个商家和食品对应的现有购物车条目。
     */
    @Select("""
            SELECT id, food_id, business_id, user_id, quantity
            FROM cart
            WHERE user_id = #{userId}
              AND business_id = #{businessId}
              AND food_id = #{foodId}
            LIMIT 1
            """)
    Cart findExisting(@Param("userId") String userId,
                      @Param("businessId") Integer businessId,
                      @Param("foodId") Integer foodId);

    /**
     * 根据用户和购物车编号查询单条记录。
     */
    @Select("""
            SELECT id, food_id, business_id, user_id, quantity
            FROM cart
            WHERE user_id = #{userId}
              AND id = #{cartId}
            """)
    Cart findByIdForUser(@Param("userId") String userId, @Param("cartId") Integer cartId);

    /**
     * 新增购物车条目。
     */
    @Insert("""
            INSERT INTO cart(food_id, business_id, user_id, quantity)
            VALUES (#{foodId}, #{businessId}, #{userId}, #{quantity})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Cart cart);

    /**
     * 累加购物车数量。
     */
    @Update("""
            UPDATE cart
            SET quantity = quantity + #{quantity}
            WHERE id = #{cartId}
            """)
    int increaseQuantity(@Param("cartId") Integer cartId, @Param("quantity") Integer quantity);

    /**
     * 修改购物车数量。
     */
    @Update("""
            UPDATE cart
            SET quantity = #{quantity}
            WHERE user_id = #{userId}
              AND id = #{cartId}
            """)
    int updateQuantity(@Param("userId") String userId,
                       @Param("cartId") Integer cartId,
                       @Param("quantity") Integer quantity);

    /**
     * 按条件删除购物车条目。
     */
    int deleteByFilter(@Param("userId") String userId,
                       @Param("businessId") Integer businessId,
                       @Param("foodId") Integer foodId);

    /**
     * 根据用户和购物车编号删除单条记录。
     */
    @Delete("""
            DELETE FROM cart
            WHERE user_id = #{userId}
              AND id = #{cartId}
            """)
    int deleteByIdForUser(@Param("userId") String userId, @Param("cartId") Integer cartId);
}
