package com.iteleme.backend.mapper;

import com.iteleme.backend.constant.OrderStatus;
import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.entity.OrderDetail;
import com.iteleme.backend.entity.Orders;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int insert(Orders order);

    Orders findById(@Param("id") Integer id);

    long count(@Param("userId") Integer userId,
               @Param("role") UserRole role,
               @Param("businessIds") List<Integer> businessIds,
               @Param("businessId") Integer businessId,
               @Param("orderStatus") OrderStatus orderStatus);

    List<Orders> list(@Param("userId") Integer userId,
                      @Param("role") UserRole role,
                      @Param("businessIds") List<Integer> businessIds,
                      @Param("businessId") Integer businessId,
                      @Param("orderStatus") OrderStatus orderStatus,
                      @Param("offset") int offset,
                      @Param("pageSize") int pageSize);

    int updateStatus(@Param("id") Integer id, @Param("orderStatus") OrderStatus orderStatus);

    int updateStatusIfMatch(@Param("id") Integer id,
                            @Param("oldStatus") OrderStatus oldStatus,
                            @Param("newStatus") OrderStatus newStatus);

    int insertDetail(OrderDetail detail);

    List<OrderDetail> details(@Param("orderId") Integer orderId);

    Integer nextId();

    Integer nextDetailId();
}
