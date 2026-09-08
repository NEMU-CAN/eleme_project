package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.OrderDetail;
import com.iteleme.backend.entity.Orders;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int insert(Orders order);

    Orders findById(@Param("id") Integer id);

    long count(@Param("userId") Integer userId,
               @Param("role") Integer role,
               @Param("businessIds") List<Integer> businessIds,
               @Param("businessId") Integer businessId,
               @Param("orderStatus") Integer orderStatus);

    List<Orders> list(@Param("userId") Integer userId,
                      @Param("role") Integer role,
                      @Param("businessIds") List<Integer> businessIds,
                      @Param("businessId") Integer businessId,
                      @Param("orderStatus") Integer orderStatus,
                      @Param("offset") int offset,
                      @Param("pageSize") int pageSize);

    int updateStatus(@Param("id") Integer id, @Param("orderStatus") Integer orderStatus);

    int insertDetail(OrderDetail detail);

    List<OrderDetail> details(@Param("orderId") Integer orderId);

    Integer nextId();

    Integer nextDetailId();
}
