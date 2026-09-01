package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.entity.Order;
import com.iteleme.backend.entity.OrderDetail;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.CartMapper;
import com.iteleme.backend.mapper.DeliveryAddressMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.mapper.OrderDetailMapper;
import com.iteleme.backend.mapper.OrderMapper;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.OrderService;
import com.iteleme.backend.vo.OrderItemVO;
import com.iteleme.backend.vo.OrderVO;
import com.iteleme.backend.vo.request.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final CartMapper cartMapper;
    private final UserMapper userMapper;
    private final BusinessMapper businessMapper;
    private final FoodMapper foodMapper;
    private final DeliveryAddressMapper deliveryAddressMapper;

    @Override
    public List<OrderVO> listOrdersByUserId(String userId, Integer businessId, Integer orderState) {
        ensureActiveUser(userId);
        ServiceValidator.requireOptionalPositive(businessId, "businessId");
        ServiceValidator.requireOptionalZeroOrOne(orderState, "orderState");
        return orderMapper.findByUserId(userId, businessId, orderState).stream()
                .map(this::assembleOrder)
                .toList();
    }

    @Override
    @Transactional
    public OrderVO createOrder(String userId, OrderCreateRequest request) {
        ensureActiveUser(userId);
        validateCreateRequest(request);

        Business business = businessMapper.findById(request.getBusinessId());
        DeliveryAddress deliveryAddress = deliveryAddressMapper.findByIdForUser(userId, request.getDaId());
        if (business == null || deliveryAddress == null) {
            throw ApiException.notFound();
        }

        List<Cart> cartItems = cartMapper.findByUserId(userId, request.getBusinessId());
        if (cartItems.isEmpty()) {
            throw ApiException.conflict("cart", "购物车为空，无法创建订单");
        }

        BigDecimal orderTotal = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            Food food = foodMapper.findByIdAndBusinessId(cartItem.getFoodId(), request.getBusinessId());
            if (food == null) {
                throw ApiException.notFound();
            }
            orderTotal = orderTotal.add(food.getFoodPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setBusinessId(request.getBusinessId());
        order.setOrderDate(LocalDateTime.now().format(ORDER_DATE_FORMATTER));
        order.setOrderTotal(orderTotal);
        order.setDaId(request.getDaId());
        order.setOrderState(0);
        orderMapper.insert(order);

        for (Cart cartItem : cartItems) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getOrderId());
            orderDetail.setFoodId(cartItem.getFoodId());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetailMapper.insert(orderDetail);
        }
        cartMapper.deleteByFilter(userId, request.getBusinessId(), null);

        Order created = orderMapper.findByIdForUser(userId, order.getOrderId());
        return assembleOrder(created);
    }

    @Override
    public OrderVO getOrderById(String userId, Integer orderId) {
        ensureActiveUser(userId);
        ServiceValidator.requirePositive(orderId, "orderId");
        Order order = orderMapper.findByIdForUser(userId, orderId);
        if (order == null) {
            throw ApiException.notFound();
        }
        return assembleOrder(order);
    }

    private void validateCreateRequest(OrderCreateRequest request) {
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requirePositive(request.getBusinessId(), "businessId");
        ServiceValidator.requirePositive(request.getDaId(), "daId");
    }

    private OrderVO assembleOrder(Order order) {
        Business business = businessMapper.findById(order.getBusinessId());
        DeliveryAddress deliveryAddress = deliveryAddressMapper.findByIdForUser(order.getUserId(), order.getDaId());
        OrderVO vo = VoConverters.toOrderVO(order, business, deliveryAddress);
        List<OrderItemVO> items = orderDetailMapper.findByOrderId(order.getOrderId()).stream()
                .map(orderDetail -> VoConverters.toOrderItemVO(orderDetail, foodMapper.findById(orderDetail.getFoodId())))
                .toList();
        vo.setItems(items);
        return vo;
    }

    private void ensureActiveUser(String userId) {
        ServiceValidator.requireUserId(userId);
        if (userMapper.findActiveById(userId) == null) {
            throw ApiException.notFound();
        }
    }
}
