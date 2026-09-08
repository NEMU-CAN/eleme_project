package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.DeliveryAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressMapper {
    List<DeliveryAddress> list(@Param("userId") Integer userId);

    DeliveryAddress findById(@Param("id") Integer id, @Param("userId") Integer userId);

    DeliveryAddress findByIdAny(@Param("id") Integer id);

    int insert(DeliveryAddress address);

    int update(DeliveryAddress address);

    int remove(@Param("id") Integer id, @Param("userId") Integer userId);

    Integer nextId();
}
