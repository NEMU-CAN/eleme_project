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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
/**
 * 订单业务实现。
 */
public class OrderServiceImpl implements OrderService {
    /** 订单日期格式。 */
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 订单表数据访问对象。 */
    @Autowired
    private OrderMapper orderMapper;
    /** 订单明细表数据访问对象。 */
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    /** 购物车表数据访问对象。 */
    @Autowired
    private CartMapper cartMapper;
    /** 用户表数据访问对象。 */
    @Autowired
    private UserMapper userMapper;
    /** 商家表数据访问对象。 */
    @Autowired
    private BusinessMapper businessMapper;
    /** 食品表数据访问对象。 */
    @Autowired
    private FoodMapper foodMapper;
    /** 送货地址表数据访问对象。 */
    @Autowired
    private DeliveryAddressMapper deliveryAddressMapper;

    /**
     * 查询用户订单列表。
     */
    @Override
    public List<OrderVO> listOrdersByUserId(String userId, Integer businessId, Integer orderState) {
        ensureActiveUser(userId);
        ServiceValidator.requireOptionalPositive(businessId, "businessId");
        ServiceValidator.requireOptionalZeroOrOne(orderState, "orderState");
        return orderMapper.findByUserId(userId, businessId, orderState).stream()
                .map(this::assembleOrder)
                .toList();
    }

    /**
     * 创建订单。
     */
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
            orderTotal = orderTotal.add(food.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setBusinessId(request.getBusinessId());
        order.setOrderDate(LocalDateTime.now().format(ORDER_DATE_FORMATTER));
        order.setOrderTotal(orderTotal);
        order.setAddressId(request.getDaId());
        order.setOrderStatus(0);
        orderMapper.insert(order);

        for (Cart cartItem : cartItems) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getId());
            orderDetail.setFoodId(cartItem.getFoodId());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetailMapper.insert(orderDetail);
        }
        cartMapper.deleteByFilter(userId, request.getBusinessId(), null);

        Order created = orderMapper.findByIdForUser(userId, order.getId());
        return assembleOrder(created);
    }

    /**
     * 查询订单详情。
     */
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

    /**
     * 支付订单；当前项目直接模拟支付成功。
     */
    @Override
    @Transactional
    public OrderVO payOrder(String userId, Integer orderId) {
        ensureActiveUser(userId);
        ServiceValidator.requirePositive(orderId, "orderId");

        Order order = orderMapper.findByIdForUser(userId, orderId);
        if (order == null) {
            throw ApiException.notFound();
        }
        if (Integer.valueOf(1).equals(order.getOrderStatus())) {
            throw ApiException.conflict("orderId", "订单已支付，不能重复支付");
        }
        if (!Integer.valueOf(0).equals(order.getOrderStatus())) {
            throw ApiException.conflict("orderId", "订单状态不允许支付");
        }

        int affectedRows = orderMapper.markAsPaid(userId, orderId);
        if (affectedRows == 0) {
            throw ApiException.conflict("orderId", "订单状态已发生变化，请重试");
        }

        return assembleOrder(orderMapper.findByIdForUser(userId, orderId));
    }

    /**
     * 校验创建订单的请求体。
     */
    private void validateCreateRequest(OrderCreateRequest request) {
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requirePositive(request.getBusinessId(), "businessId");
        ServiceValidator.requirePositive(request.getDaId(), "daId");
    }

    /**
     * 组装订单展示对象。
     */
    private OrderVO assembleOrder(Order order) {
        Business business = businessMapper.findById(order.getBusinessId());
        DeliveryAddress deliveryAddress = deliveryAddressMapper.findByIdForUser(order.getUserId(), order.getAddressId());
        OrderVO vo = VoConverters.toOrderVO(order, business, deliveryAddress);
        List<OrderItemVO> items = orderDetailMapper.findByOrderId(order.getId()).stream()
                .map(orderDetail -> VoConverters.toOrderItemVO(orderDetail, foodMapper.findById(orderDetail.getFoodId())))
                .toList();
        vo.setItems(items);
        return vo;
    }

    /**
     * 确认用户存在且处于正常状态。
     */
    private void ensureActiveUser(String userId) {
        ServiceValidator.requireUserId(userId);
        if (userMapper.findActiveById(userId) == null) {
            throw ApiException.notFound();
        }
    }
}
