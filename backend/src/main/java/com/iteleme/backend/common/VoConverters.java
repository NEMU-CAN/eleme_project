package com.iteleme.backend.common;

import com.iteleme.backend.entity.Business;
import org.springframework.beans.BeanUtils;
import com.iteleme.backend.entity.Cart;
import com.iteleme.backend.entity.DeliveryAddress;
import com.iteleme.backend.entity.Food;
import com.iteleme.backend.entity.Order;
import com.iteleme.backend.entity.OrderDetail;
import com.iteleme.backend.entity.User;
import com.iteleme.backend.vo.BusinessVO;
import com.iteleme.backend.vo.CartItemVO;
import com.iteleme.backend.vo.DeliveryAddressVO;
import com.iteleme.backend.vo.FoodVO;
import com.iteleme.backend.vo.OrderItemVO;
import com.iteleme.backend.vo.OrderVO;
import com.iteleme.backend.vo.UserVO;

// ===== [重构] 扁平 VO 改用 Spring BeanUtils.copyProperties 按同名属性批量复制，消除手写 set 样板化 =====
/**
 * 实体与 VO 的转换工具类。
 */
public final class VoConverters {
    private VoConverters() {
    }

    /**
     * 将商家实体转换为对外展示对象。
     */
    public static BusinessVO toBusinessVO(Business business) {
        if (business == null) {
            return null;
        }
        BusinessVO vo = new BusinessVO();
        BeanUtils.copyProperties(business, vo);
        return vo;
    }

    /**
     * 将食品实体转换为对外展示对象。
     */
    public static FoodVO toFoodVO(Food food) {
        if (food == null) {
            return null;
        }
        FoodVO vo = new FoodVO();
        BeanUtils.copyProperties(food, vo);
        return vo;
    }

    /**
     * 将购物车实体和关联信息转换为对外展示对象。
     */
    public static CartItemVO toCartItemVO(Cart cart, Business business, Food food) {
        CartItemVO vo = new CartItemVO();
        vo.setId(cart.getId());
        vo.setUserId(cart.getUserId());
        vo.setBusinessId(cart.getBusinessId());
        vo.setFoodId(cart.getFoodId());
        vo.setQuantity(cart.getQuantity());
        vo.setBusiness(toBusinessVO(business));
        vo.setFood(toFoodVO(food));
        return vo;
    }

    /**
     * 将送货地址实体转换为对外展示对象。
     */
    public static DeliveryAddressVO toDeliveryAddressVO(DeliveryAddress deliveryAddress) {
        if (deliveryAddress == null) {
            return null;
        }
        DeliveryAddressVO vo = new DeliveryAddressVO();
        BeanUtils.copyProperties(deliveryAddress, vo);
        return vo;
    }

    // ===== [重构] 从订单的收货地址快照组装展示对象（历史订单不依赖地址行） =====
    /**
     * 将订单的收货地址快照转换为对外展示对象。
     */
    public static DeliveryAddressVO toDeliveryAddressVO(Order order) {
        if (order == null) {
            return null;
        }
        DeliveryAddressVO vo = new DeliveryAddressVO();
        vo.setId(order.getAddressId());
        vo.setContactName(order.getAddressContactName());
        vo.setContactSex(order.getAddressContactSex());
        vo.setContactTel(order.getAddressContactTel());
        vo.setAddress(order.getAddressDetail());
        vo.setUserId(order.getUserId());
        return vo;
    }
    // ===== [重构结束] =====

    /**
     * 将订单明细实体转换为对外展示对象。
     */
    public static OrderItemVO toOrderItemVO(OrderDetail orderDetail, Food food) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(orderDetail.getId());
        vo.setOrderId(orderDetail.getOrderId());
        vo.setFoodId(orderDetail.getFoodId());
        vo.setQuantity(orderDetail.getQuantity());
        vo.setFood(toFoodVO(food));
        return vo;
    }

    /**
     * 将订单实体和关联信息转换为对外展示对象。
     */
    public static OrderVO toOrderVO(Order order, Business business, DeliveryAddressVO deliveryAddress) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setUserId(order.getUserId());
        vo.setBusinessId(order.getBusinessId());
        vo.setOrderDate(order.getOrderDate());
        vo.setOrderTotal(order.getOrderTotal());
        vo.setAddressId(order.getAddressId());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setBusiness(toBusinessVO(business));
        vo.setDeliveryAddress(deliveryAddress);
        return vo;
    }

    /**
     * 将用户实体转换为对外展示对象。
     */
    public static UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
