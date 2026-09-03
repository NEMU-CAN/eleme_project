package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Business;
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

/**
 * 实体与 VO 的转换工具类。
 */
final class VoConverters {
    private VoConverters() {
    }

    /**
     * 将商家实体转换为对外展示对象。
     */
    static BusinessVO toBusinessVO(Business business) {
        if (business == null) {
            return null;
        }
        BusinessVO vo = new BusinessVO();
        vo.setId(business.getId());
        vo.setName(business.getName());
        vo.setAddress(business.getAddress());
        vo.setDescription(business.getDescription());
        vo.setImage(business.getImage());
        vo.setOrderTypeId(business.getOrderTypeId());
        vo.setStartPrice(business.getStartPrice());
        vo.setDeliveryPrice(business.getDeliveryPrice());
        vo.setRemark(business.getRemark());
        return vo;
    }

    /**
     * 将食品实体转换为对外展示对象。
     */
    static FoodVO toFoodVO(Food food) {
        if (food == null) {
            return null;
        }
        FoodVO vo = new FoodVO();
        vo.setId(food.getId());
        vo.setName(food.getName());
        vo.setDescription(food.getDescription());
        vo.setImage(food.getImage());
        vo.setPrice(food.getPrice());
        vo.setBusinessId(food.getBusinessId());
        vo.setRemark(food.getRemark());
        return vo;
    }

    /**
     * 将购物车实体和关联信息转换为对外展示对象。
     */
    static CartItemVO toCartItemVO(Cart cart, Business business, Food food) {
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
    static DeliveryAddressVO toDeliveryAddressVO(DeliveryAddress deliveryAddress) {
        if (deliveryAddress == null) {
            return null;
        }
        DeliveryAddressVO vo = new DeliveryAddressVO();
        vo.setId(deliveryAddress.getId());
        vo.setContactName(deliveryAddress.getContactName());
        vo.setContactSex(deliveryAddress.getContactSex());
        vo.setContactTel(deliveryAddress.getContactTel());
        vo.setAddress(deliveryAddress.getAddress());
        vo.setUserId(deliveryAddress.getUserId());
        return vo;
    }

    /**
     * 将订单明细实体转换为对外展示对象。
     */
    static OrderItemVO toOrderItemVO(OrderDetail orderDetail, Food food) {
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
    static OrderVO toOrderVO(Order order, Business business, DeliveryAddress deliveryAddress) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setUserId(order.getUserId());
        vo.setBusinessId(order.getBusinessId());
        vo.setOrderDate(order.getOrderDate());
        vo.setOrderTotal(order.getOrderTotal());
        vo.setAddressId(order.getAddressId());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setBusiness(toBusinessVO(business));
        vo.setDeliveryAddress(toDeliveryAddressVO(deliveryAddress));
        return vo;
    }

    /**
     * 将用户实体转换为对外展示对象。
     */
    static UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setSex(user.getSex());
        vo.setAvatar(user.getAvatar());
        vo.setDelFlag(user.getDelFlag());
        return vo;
    }
}
