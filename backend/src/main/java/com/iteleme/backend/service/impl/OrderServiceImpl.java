package com.iteleme.backend.service.impl;

import com.iteleme.backend.common.FieldErrorVO;
import com.iteleme.backend.common.PageResult;
import com.iteleme.backend.constant.BusinessStatus;
import com.iteleme.backend.constant.FoodStatus;
import com.iteleme.backend.constant.OrderStatus;
import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.dto.OrderCreateRequest;
import com.iteleme.backend.dto.OrderStatusRequest;
import com.iteleme.backend.entity.Business;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.entity.OrderDetail;
import com.iteleme.backend.entity.Orders;
import com.iteleme.backend.exception.BadRequestException;
import com.iteleme.backend.exception.ConflictException;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.mapper.AddressMapper;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.CartMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.mapper.OrderMapper;
import com.iteleme.backend.service.AccessService;
import com.iteleme.backend.service.OrderService;
import com.iteleme.backend.vo.DeliveryAddressVO;
import com.iteleme.backend.vo.OrderDetailVO;
import com.iteleme.backend.vo.OrderSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final FoodMapper foodMapper;
    private final AddressMapper addressMapper;
    private final BusinessMapper businessMapper;
    private final AccessService accessService;

    @Override
    @Transactional
    public OrderDetailVO create(OrderCreateRequest request) {
        ensureCustomer();
        Integer userId = CurrentUserContext.userId();
        List<Cart> carts = cartMapper.list(userId, request.businessId());
        if (carts.isEmpty()) {
            throw new ConflictException("购物车为空", List.of(new FieldErrorVO("cart", "购物车为空")));
        }

        Business business = loadBusiness(request.businessId());
        if (business.getStatus() != BusinessStatus.OPEN) {
            throw new ConflictException("商家未营业", List.of(new FieldErrorVO("businessId", "商家未营业")));
        }
        DeliveryAddress address = loadAddress(request.deliveryAddressId());
        List<OrderDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : carts) {
            Food food = loadFood(cart.getFoodId());
            if (!food.getBusinessId().equals(request.businessId())) {
                throw new BadRequestException("购物车数据异常");
            }
            if (food.getStatus() != FoodStatus.ONLINE) {
                throw new ConflictException("商品已下架", List.of(new FieldErrorVO("foodId", "商品已下架")));
            }
            if (food.getStock() < cart.getQuantity()) {
                throw new ConflictException("库存不足", List.of(new FieldErrorVO("stock", "库存不足")));
            }
            BigDecimal subtotal = food.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            details.add(buildDetail(food, cart.getQuantity(), subtotal));
        }

        Orders order = new Orders(
                orderMapper.nextId(),
                buildOrderNo(),
                userId,
                business.getId(),
                LocalDateTime.now(),
                business.getDeliveryPrice(),
                totalAmount,
                totalAmount.add(business.getDeliveryPrice()),
                address.getId(),
                OrderStatus.UNPAID
        );
        orderMapper.insert(order);

        for (OrderDetail detail : details) {
            detail.setOrderId(order.getId());
            detail.setId(orderMapper.nextDetailId());
            orderMapper.insertDetail(detail);
            int affected = foodMapper.deduct(detail.getFoodId(), detail.getQuantity());
            if (affected == 0) {
                throw new ConflictException("库存不足", List.of(new FieldErrorVO("stock", "库存不足")));
            }
        }

        cartMapper.clearByUserAndBusiness(userId, request.businessId());
        return get(order.getId());
    }

    @Override
    public PageResult<OrderSummaryVO> list(Integer businessId, Integer orderStatus, int page, int pageSize) {
        validateOrderStatus(orderStatus);
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        Integer userId = CurrentUserContext.userId();
        List<Integer> ownedBusinessIds = null;
        if (CurrentUserContext.role() == UserRole.BUSINESS) {
            ownedBusinessIds = accessService.ownedBusinessIds(userId);
            if (businessId != null && !ownedBusinessIds.contains(businessId)) {
                throw new ForbiddenException("无权查看该商家的订单");
            }
            if (ownedBusinessIds.isEmpty() && businessId == null) {
                return PageResult.of(0, page, pageSize, List.of());
            }
        }
        long total = orderMapper.count(
                userId,
                CurrentUserContext.role(),
                ownedBusinessIds,
                businessId,
                orderStatus
        );
        List<Orders> orders = orderMapper.list(
                userId,
                CurrentUserContext.role(),
                ownedBusinessIds,
                businessId,
                orderStatus,
                (page - 1) * pageSize,
                pageSize
        );
        List<OrderSummaryVO> records = new ArrayList<>(orders.size());
        for (Orders order : orders) {
            records.add(toSummary(order));
        }
        return PageResult.of(total, page, pageSize, records);
    }

    @Override
    public OrderDetailVO get(Integer id) {
        Orders order = loadOrder(id);
        ensureReadable(order);
        DeliveryAddress address = addressMapper.findByIdAny(order.getDeliveryAddressId());
        return new OrderDetailVO(order, address, orderMapper.details(order.getId()));
    }

    @Override
    @Transactional
    public void status(Integer id, OrderStatusRequest request) {
        Orders order = loadOrder(id);
        ensureReadable(order);
        if (request.orderStatus() != OrderStatus.CANCELED
                && request.orderStatus() != OrderStatus.UNPAID
                && request.orderStatus() != OrderStatus.PAID
                && request.orderStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("非法订单状态");
        }
        orderMapper.updateStatus(id, request.orderStatus());
    }

    private void ensureCustomer() {
        if (CurrentUserContext.role() != UserRole.CUSTOMER) {
            throw new ForbiddenException("无权限操作");
        }
    }

    private Orders loadOrder(Integer id) {
        Orders order = orderMapper.findById(id);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }
        return order;
    }

    private void ensureReadable(Orders order) {
        Integer role = CurrentUserContext.role();
        if (role == UserRole.ADMIN) {
            return;
        }
        if (role == UserRole.CUSTOMER && !order.getUserId().equals(CurrentUserContext.userId())) {
            throw new ForbiddenException("无权查看订单");
        }
        if (role == UserRole.BUSINESS) {
            List<Integer> ownedBusinessIds = accessService.ownedBusinessIds(CurrentUserContext.userId());
            if (!ownedBusinessIds.contains(order.getBusinessId())) {
                throw new ForbiddenException("无权查看订单");
            }
        }
    }

    private Business loadBusiness(Integer businessId) {
        Business business = businessMapper.findById(businessId);
        if (business == null || business.getStatus() == BusinessStatus.DELETED) {
            throw new NotFoundException("商家不存在");
        }
        return business;
    }

    private DeliveryAddress loadAddress(Integer addressId) {
        DeliveryAddress address = addressMapper.findByIdAny(addressId);
        if (address == null) {
            throw new NotFoundException("收货地址不存在");
        }
        if (!address.getUserId().equals(CurrentUserContext.userId())) {
            throw new ForbiddenException("无权使用该地址");
        }
        return address;
    }

    private Food loadFood(Integer foodId) {
        Food food = foodMapper.findById(foodId);
        if (food == null) {
            throw new NotFoundException("商品不存在");
        }
        return food;
    }

    private OrderDetail buildDetail(Food food, Integer quantity, BigDecimal subtotal) {
        return new OrderDetail(
                null,
                null,
                food.getId(),
                quantity,
                food.getName(),
                food.getPrice(),
                subtotal
        );
    }

    private OrderSummaryVO toSummary(Orders order) {
        Business business = businessMapper.findById(order.getBusinessId());
        DeliveryAddress address = addressMapper.findByIdAny(order.getDeliveryAddressId());
        List<OrderDetail> details = orderMapper.details(order.getId());
        DeliveryAddressVO addressVO = address == null ? null : DeliveryAddressVO.from(address);
        return new OrderSummaryVO(order, business, addressVO, details == null ? 0 : details.size());
    }

    private void validateOrderStatus(Integer orderStatus) {
        if (orderStatus == null) {
            return;
        }
        if (orderStatus != OrderStatus.CANCELED
                && orderStatus != OrderStatus.UNPAID
                && orderStatus != OrderStatus.PAID
                && orderStatus != OrderStatus.COMPLETED) {
            throw new BadRequestException("非法订单状态");
        }
    }

    private String buildOrderNo() {
        return "O" + System.currentTimeMillis() + CurrentUserContext.userId();
    }
}
