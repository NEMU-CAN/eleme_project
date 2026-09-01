package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    @Select("""
            SELECT odId, orderId, foodId, quantity
            FROM orderdetailet
            WHERE orderId = #{orderId}
            ORDER BY odId
            """)
    List<OrderDetail> findByOrderId(@Param("orderId") Integer orderId);

    @Insert("""
            INSERT INTO orderdetailet(orderId, foodId, quantity)
            VALUES (#{orderId}, #{foodId}, #{quantity})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "odId")
    int insert(OrderDetail orderDetail);
}
