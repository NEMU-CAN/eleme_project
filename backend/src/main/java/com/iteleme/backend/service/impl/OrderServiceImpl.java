package com.iteleme.backend.service.impl;

import com.iteleme.backend.common.ServiceValidator;
import com.iteleme.backend.entity.Order;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.OrderMapper;
import com.iteleme.backend.service.OrderService;
import com.iteleme.backend.service.support.OrderAssembler;
import com.iteleme.backend.service.support.OrderContext;
import com.iteleme.backend.service.support.OrderPriceCalculator;
import com.iteleme.backend.service.support.OrderValidator;
import com.iteleme.backend.service.support.OrderWriter;
import com.iteleme.backend.service.support.UserValidator;
import com.iteleme.backend.vo.OrderVO;
import com.iteleme.backend.vo.request.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
/**
 * 订单业务实现。
 */
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    /** 订单表数据访问对象。 */
    private final OrderMapper orderMapper;
    // ===== [重构] 协作组件：校验/加载/计价/写库/组装 =====
    /** 用户校验器（纵深防御：确认用户存在且有效）。 */
    private final UserValidator userValidator;
    /** 下单前置校验器（加载并校验商家/地址/购物车）。 */
    private final OrderValidator orderValidator;
    /** 订单金额计算器。 */
    private final OrderPriceCalculator orderPriceCalculator;
    /** 订单写入器（建订单+插明细+删购物车）。 */
    private final OrderWriter orderWriter;
    /** 订单 VO 组装器。 */
    private final OrderAssembler orderAssembler;
    // ===== [重构结束] =====

    /**
     * 查询用户订单列表。
     */
    @Override
    public List<OrderVO> listOrdersByUserId(String userId, Integer businessId, Integer orderState) {
        userValidator.requireActive(userId);
        ServiceValidator.requireOptionalPositive(businessId, "businessId");
        ServiceValidator.requireOptionalZeroOrOne(orderState, "orderState");
        return orderMapper.findByUserId(userId, businessId, orderState).stream()
                .map(orderAssembler::assemble)
                .toList();
    }

    /**
     * 创建订单。
     */
    @Override
    @Transactional
    public OrderVO createOrder(String userId, OrderCreateRequest request) {
        userValidator.requireActive(userId);
        validateCreateRequest(request);

        OrderContext ctx = orderValidator.validate(userId, request);
        BigDecimal foodTotal = orderPriceCalculator.foodTotal(ctx.cartItems(), request.getBusinessId());
        orderPriceCalculator.ensureMeetStartPrice(ctx.business(), foodTotal);
        BigDecimal orderTotal = orderPriceCalculator.orderTotal(ctx.business(), foodTotal);

        Order created = orderWriter.write(userId, request, orderTotal, ctx.cartItems());
        return orderAssembler.assemble(created);
    }

    /**
     * 查询订单详情。
     */
    @Override
    public OrderVO getOrderById(String userId, Integer orderId) {
        userValidator.requireActive(userId);
        ServiceValidator.requirePositive(orderId, "orderId");
        Order order = orderMapper.findByIdForUser(userId, orderId);
        if (order == null) {
            throw ApiException.notFound();
        }
        return orderAssembler.assemble(order);
    }

    /**
     * 支付订单；当前项目直接模拟支付成功。
     */
    @Override
    @Transactional
    public OrderVO payOrder(String userId, Integer orderId) {
        userValidator.requireActive(userId);
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

        return orderAssembler.assemble(orderMapper.findByIdForUser(userId, orderId));
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
}
