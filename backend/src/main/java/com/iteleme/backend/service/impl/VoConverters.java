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

final class VoConverters {
    private VoConverters() {
    }

    static BusinessVO toBusinessVO(Business business) {
        if (business == null) {
            return null;
        }
        BusinessVO vo = new BusinessVO();
        vo.setBusinessId(business.getBusinessId());
        vo.setBusinessName(business.getBusinessName());
        vo.setBusinessAddress(business.getBusinessAddress());
        vo.setBusinessExplain(business.getBusinessExplain());
        vo.setBusinessImg(business.getBusinessImg());
        vo.setOrderTypeId(business.getOrderTypeId());
        vo.setStarPrice(business.getStarPrice());
        vo.setDeliveryPrice(business.getDeliveryPrice());
        vo.setRemarks(business.getRemarks());
        return vo;
    }

    static FoodVO toFoodVO(Food food) {
        if (food == null) {
            return null;
        }
        FoodVO vo = new FoodVO();
        vo.setFoodId(food.getFoodId());
        vo.setFoodName(food.getFoodName());
        vo.setFoodExplain(food.getFoodExplain());
        vo.setFoodImg(food.getFoodImg());
        vo.setFoodPrice(food.getFoodPrice());
        vo.setBusinessId(food.getBusinessId());
        vo.setRemarks(food.getRemarks());
        return vo;
    }

    static CartItemVO toCartItemVO(Cart cart, Business business, Food food) {
        CartItemVO vo = new CartItemVO();
        vo.setCartId(cart.getCartId());
        vo.setUserId(cart.getUserId());
        vo.setBusinessId(cart.getBusinessId());
        vo.setFoodId(cart.getFoodId());
        vo.setQuantity(cart.getQuantity());
        vo.setBusiness(toBusinessVO(business));
        vo.setFood(toFoodVO(food));
        return vo;
    }

    static DeliveryAddressVO toDeliveryAddressVO(DeliveryAddress deliveryAddress) {
        if (deliveryAddress == null) {
            return null;
        }
        DeliveryAddressVO vo = new DeliveryAddressVO();
        vo.setDaId(deliveryAddress.getDaId());
        vo.setContactName(deliveryAddress.getContactName());
        vo.setContactSex(deliveryAddress.getContactSex());
        vo.setContactTel(deliveryAddress.getContactTel());
        vo.setAddress(deliveryAddress.getAddress());
        vo.setUserId(deliveryAddress.getUserId());
        return vo;
    }

    static OrderItemVO toOrderItemVO(OrderDetail orderDetail, Food food) {
        OrderItemVO vo = new OrderItemVO();
        vo.setOdId(orderDetail.getOdId());
        vo.setOrderId(orderDetail.getOrderId());
        vo.setFoodId(orderDetail.getFoodId());
        vo.setQuantity(orderDetail.getQuantity());
        vo.setFood(toFoodVO(food));
        return vo;
    }

    static OrderVO toOrderVO(Order order, Business business, DeliveryAddress deliveryAddress) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setUserId(order.getUserId());
        vo.setBusinessId(order.getBusinessId());
        vo.setOrderDate(order.getOrderDate());
        vo.setOrderTotal(order.getOrderTotal());
        vo.setDaId(order.getDaId());
        vo.setOrderState(order.getOrderState());
        vo.setBusiness(toBusinessVO(business));
        vo.setDeliveryAddress(toDeliveryAddressVO(deliveryAddress));
        return vo;
    }

    static UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setUserSex(user.getUserSex());
        vo.setUserImg(user.getUserImg());
        vo.setDelTag(user.getDelTag());
        return vo;
    }
}
