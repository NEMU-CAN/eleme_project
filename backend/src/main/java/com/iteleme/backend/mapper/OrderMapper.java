package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单数据访问接口。
 */
@Mapper
public interface OrderMapper {
    /**
     * 查询某个用户的订单列表，可按商家和状态筛选。
     */
    List<Order> findByUserId(@Param("userId") String userId,
                             @Param("businessId") Integer businessId,
                             @Param("orderState") Integer orderState);

    /**
     * 根据用户和订单编号查询订单。
     */
    @Select("""
            SELECT id, user_id, business_id, order_date, order_total, address_id, order_status
            FROM orders
            WHERE user_id = #{userId}
              AND id = #{orderId}
            """)
    Order findByIdForUser(@Param("userId") String userId, @Param("orderId") Integer orderId);

    /**
     * 新增订单。
     */
    @Insert("""
            INSERT INTO orders(user_id, business_id, order_date, order_total, address_id, order_status)
            VALUES (#{userId}, #{businessId}, #{orderDate}, #{orderTotal}, #{addressId}, #{orderStatus})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);
}
