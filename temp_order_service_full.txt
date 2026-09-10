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
import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.BadRequestException;
import com.iteleme.backend.exception.ConflictException;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.mapper.AddressMapper;
import com.iteleme.backend.mapper.BusinessMapper;
import com.iteleme.backend.mapper.CartMapper;
import com.iteleme.backend.mapper.FoodMapper;
import com.iteleme.backend.mapper.OrderMapper;
import com.iteleme.backend.mapper.UserMapper;
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
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final FoodMapper foodMapper;
    private final AddressMapper addressMapper;
    private final BusinessMapper businessMapper;
    private final UserMapper userMapper;
    private final AccessService accessService;

    @Override
    @Transactional
    public OrderDetailVO create(OrderCreateRequest request) {
        ensureCustomer();
        Integer userId = CurrentUserContext.userId();
        User user = loadCurrentUser();
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

        // 先预占库存，确保未支付订单已经锁定商品数量。
        reserveStocks(details);

        Orders order = new Orders(
                orderMapper.nextId(),
                buildOrderNo(),
                userId,
                business.getId(),
                user.getNickname(),
                user.getPhone(),
                business.getName(),
                business.getAddress(),
                address.getContactName(),
                address.getContactTel(),
                address.getContactGender(),
                address.getAddress(),
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
        return new OrderDetailVO(order, snapshotAddress(order), orderMapper.details(order.getId()));
    }

    @Override
    @Transactional
    public void status(Integer id, OrderStatusRequest request) {
        Orders order = loadOrder(id);
        ensureReadable(order);
        Integer targetStatus = request.orderStatus();
        validateStatusValue(targetStatus);
        ensureStatusTransitionAllowed(order, targetStatus);
        if (Objects.equals(order.getOrderStatus(), targetStatus)) {
            throw new BadRequestException("订单状态未发生变化");
        }

        int affected = orderMapper.updateStatusIfMatch(order.getId(), order.getOrderStatus(), targetStatus);
        if (affected == 0) {
            throw new ConflictException("订单状态已变化");
        }

        if (targetStatus == OrderStatus.PAID) {
            consumeReservedStocks(order);
        } else if (targetStatus == OrderStatus.CANCELED) {
            releaseReservedStocks(order);
        }
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

    private User loadCurrentUser() {
        User user = userMapper.findById(CurrentUserContext.userId());
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        if (user.getStatus() != 0) {
            throw new ForbiddenException("账号已禁用");
        }
        return user;
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
        List<OrderDetail> details = orderMapper.details(order.getId());
        Business business = snapshotBusiness(order);
        DeliveryAddressVO addressVO = DeliveryAddressVO.from(snapshotAddress(order));
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

    private void validateStatusValue(Integer orderStatus) {
        if (orderStatus == null
                || (orderStatus != OrderStatus.CANCELED
                && orderStatus != OrderStatus.UNPAID
                && orderStatus != OrderStatus.PAID
                && orderStatus != OrderStatus.COMPLETED)) {
            throw new BadRequestException("非法订单状态");
        }
    }

    private void ensureStatusTransitionAllowed(Orders order, Integer targetStatus) {
        Integer role = CurrentUserContext.role();
        if (role == UserRole.CUSTOMER) {
            if (!order.getUserId().equals(CurrentUserContext.userId())) {
                throw new ForbiddenException("无权修改订单");
            }
            if (targetStatus != OrderStatus.PAID && targetStatus != OrderStatus.CANCELED) {
                throw new BadRequestException("当前订单状态不允许这样修改");
            }
            validateLifecycleTransition(order, targetStatus);
        } else if (role == UserRole.BUSINESS) {
            List<Integer> ownedBusinessIds = accessService.ownedBusinessIds(CurrentUserContext.userId());
            if (!ownedBusinessIds.contains(order.getBusinessId())) {
                throw new ForbiddenException("无权修改订单");
            }
            if (order.getOrderStatus() != OrderStatus.PAID || targetStatus != OrderStatus.COMPLETED) {
                throw new BadRequestException("当前订单状态不允许这样修改");
            }
        } else if (role == UserRole.ADMIN) {
            validateLifecycleTransition(order, targetStatus);
        } else {
            throw new ForbiddenException("无权修改订单");
        }
    }

    private void validateLifecycleTransition(Orders order, Integer targetStatus) {
        if (order.getOrderStatus() == OrderStatus.UNPAID
                && (targetStatus == OrderStatus.PAID || targetStatus == OrderStatus.CANCELED)) {
            return;
        }
        if (order.getOrderStatus() == OrderStatus.PAID && targetStatus == OrderStatus.COMPLETED) {
            return;
        }
        throw new BadRequestException("当前订单状态不允许这样修改");
    }

    private void reserveStocks(List<OrderDetail> details) {
        for (OrderDetail detail : details) {
            int affected = foodMapper.reserveStock(detail.getFoodId(), detail.getQuantity());
            if (affected == 0) {
                throw new ConflictException("库存不足", List.of(new FieldErrorVO("stock", "库存不足")));
            }
        }
    }

    private void consumeReservedStocks(Orders order) {
        for (OrderDetail detail : orderMapper.details(order.getId())) {
            int affected = foodMapper.consumeReservedStock(detail.getFoodId(), detail.getQuantity());
            if (affected == 0) {
                throw new ConflictException("库存不足", List.of(new FieldErrorVO("stock", "库存不足")));
            }
        }
    }

    private void releaseReservedStocks(Orders order) {
        for (OrderDetail detail : orderMapper.details(order.getId())) {
            int affected = foodMapper.releaseReservedStock(detail.getFoodId(), detail.getQuantity());
            if (affected == 0) {
                throw new ConflictException("库存不足", List.of(new FieldErrorVO("stock", "库存不足")));
            }
        }
    }

    private DeliveryAddress snapshotAddress(Orders order) {
        return new DeliveryAddress(
                order.getDeliveryAddressId(),
                order.getUserId(),
                order.getReceiverAddress(),
                order.getReceiverName(),
                order.getReceiverTel(),
                order.getReceiverGender(),
                0
        );
    }

    private Business snapshotBusiness(Orders order) {
        // 订单详情和列表都只读快照，避免商家后续改名改地址影响历史订单。
        return new Business(
                order.getBusinessId(),
                order.getBusinessName(),
                order.getBusinessAddress(),
                null,
                null,
                null,
                null,
                order.getDeliveryPrice(),
                null
        );
    }

    private String buildOrderNo() {
        return "O" + System.currentTimeMillis() + CurrentUserContext.userId() + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
