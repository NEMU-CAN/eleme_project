package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {
    @Select("""
            <script>
            SELECT orderId, userId, businessId, orderDate, orderTotal, daId, orderState
            FROM orders
            WHERE userId = #{userId}
            <if test="businessId != null">
                AND businessId = #{businessId}
            </if>
            <if test="orderState != null">
                AND orderState = #{orderState}
            </if>
            ORDER BY orderId DESC
            </script>
            """)
    List<Order> findByUserId(@Param("userId") String userId,
                             @Param("businessId") Integer businessId,
                             @Param("orderState") Integer orderState);

    @Select("""
            SELECT orderId, userId, businessId, orderDate, orderTotal, daId, orderState
            FROM orders
            WHERE userId = #{userId}
              AND orderId = #{orderId}
            """)
    Order findByIdForUser(@Param("userId") String userId, @Param("orderId") Integer orderId);

    @Insert("""
            INSERT INTO orders(userId, businessId, orderDate, orderTotal, daId, orderState)
            VALUES (#{userId}, #{businessId}, #{orderDate}, #{orderTotal}, #{daId}, #{orderState})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int insert(Order order);
}
