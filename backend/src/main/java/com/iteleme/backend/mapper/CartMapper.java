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

@Mapper
public interface CartMapper {
    @Select("""
            <script>
            SELECT cartId, foodId, businessId, userId, quantity
            FROM cart
            WHERE userId = #{userId}
            <if test="businessId != null">
                AND businessId = #{businessId}
            </if>
            ORDER BY cartId
            </script>
            """)
    List<Cart> findByUserId(@Param("userId") String userId, @Param("businessId") Integer businessId);

    @Select("""
            SELECT cartId, foodId, businessId, userId, quantity
            FROM cart
            WHERE userId = #{userId}
              AND businessId = #{businessId}
              AND foodId = #{foodId}
            LIMIT 1
            """)
    Cart findExisting(@Param("userId") String userId,
                      @Param("businessId") Integer businessId,
                      @Param("foodId") Integer foodId);

    @Select("""
            SELECT cartId, foodId, businessId, userId, quantity
            FROM cart
            WHERE userId = #{userId}
              AND cartId = #{cartId}
            """)
    Cart findByIdForUser(@Param("userId") String userId, @Param("cartId") Integer cartId);

    @Insert("""
            INSERT INTO cart(foodId, businessId, userId, quantity)
            VALUES (#{foodId}, #{businessId}, #{userId}, #{quantity})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "cartId")
    int insert(Cart cart);

    @Update("""
            UPDATE cart
            SET quantity = quantity + #{quantity}
            WHERE cartId = #{cartId}
            """)
    int increaseQuantity(@Param("cartId") Integer cartId, @Param("quantity") Integer quantity);

    @Update("""
            UPDATE cart
            SET quantity = #{quantity}
            WHERE userId = #{userId}
              AND cartId = #{cartId}
            """)
    int updateQuantity(@Param("userId") String userId,
                       @Param("cartId") Integer cartId,
                       @Param("quantity") Integer quantity);

    @Delete("""
            <script>
            DELETE FROM cart
            WHERE userId = #{userId}
            <if test="businessId != null">
                AND businessId = #{businessId}
            </if>
            <if test="foodId != null">
                AND foodId = #{foodId}
            </if>
            </script>
            """)
    int deleteByFilter(@Param("userId") String userId,
                       @Param("businessId") Integer businessId,
                       @Param("foodId") Integer foodId);

    @Delete("""
            DELETE FROM cart
            WHERE userId = #{userId}
              AND cartId = #{cartId}
            """)
    int deleteByIdForUser(@Param("userId") String userId, @Param("cartId") Integer cartId);
}
