package com.iteleme.backend.service;

import com.iteleme.backend.vo.OrderVO;
import com.iteleme.backend.vo.request.OrderCreateRequest;

import java.util.List;

public interface OrderService {
    List<OrderVO> listOrdersByUserId(String userId, Integer businessId, Integer orderState);

    OrderVO createOrder(String userId, OrderCreateRequest request);

    OrderVO getOrderById(String userId, Integer orderId);

    /**
     * 支付订单。
     *
     * @param userId 用户编号
     * @param orderId 订单编号
     * @return 支付成功后的订单信息
     */
    OrderVO payOrder(String userId, Integer orderId);
}
