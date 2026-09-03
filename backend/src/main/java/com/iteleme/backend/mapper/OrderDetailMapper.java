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
            SELECT odId AS id, orderId, foodId, quantity
            FROM orderdetailet
            WHERE orderId = #{orderId}
            ORDER BY odId
            """)
    List<OrderDetail> findByOrderId(@Param("orderId") Integer orderId);

    /**
     * 新增订单明细。
     */
    @Insert("""
            INSERT INTO orderdetailet(orderId, foodId, quantity)
            VALUES (#{orderId}, #{foodId}, #{quantity})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "odId")
    int insert(OrderDetail orderDetail);
}
