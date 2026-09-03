package com.iteleme.backend.service;

import com.iteleme.backend.vo.OrderVO;
import com.iteleme.backend.vo.request.OrderCreateRequest;

import java.util.List;

public interface OrderService {
    List<OrderVO> listOrdersByUserId(String userId, Integer businessId, Integer orderState);

    OrderVO createOrder(String userId, OrderCreateRequest request);

    OrderVO getOrderById(String userId, Integer orderId);
}
