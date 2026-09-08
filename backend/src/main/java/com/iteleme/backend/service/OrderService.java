package com.iteleme.backend.service;

import com.iteleme.backend.common.PageResult;
import com.iteleme.backend.dto.OrderCreateRequest;
import com.iteleme.backend.dto.OrderStatusRequest;
import com.iteleme.backend.vo.OrderDetailVO;
import com.iteleme.backend.vo.OrderSummaryVO;

public interface OrderService {
    OrderDetailVO create(OrderCreateRequest request);

    PageResult<OrderSummaryVO> list(Integer businessId, Integer orderStatus, int page, int pageSize);

    OrderDetailVO get(Integer id);

    void status(Integer id, OrderStatusRequest request);
}
