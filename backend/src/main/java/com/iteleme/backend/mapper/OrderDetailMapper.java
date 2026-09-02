package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单明细数据访问接口。
 */
@Mapper
public interface OrderDetailMapper {
    /**
     * 查询订单对应的所有明细。
     */
    @Select("""
            SELECT id, order_id, food_id, quantity
            FROM orderdetailet
            WHERE order_id = #{orderId}
            ORDER BY id
            """)
    List<OrderDetail> findByOrderId(@Param("orderId") Integer orderId);

    /**
     * 新增订单明细。
     */
    @Insert("""
            INSERT INTO orderdetailet(order_id, food_id, quantity)
            VALUES (#{orderId}, #{foodId}, #{quantity})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderDetail orderDetail);
}
