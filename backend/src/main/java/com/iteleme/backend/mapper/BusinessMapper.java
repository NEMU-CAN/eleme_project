package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Business;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BusinessMapper {
    List<Business> list(@Param("orderTypeId") Integer orderTypeId);

    @Select("""
            SELECT id, name, address, description, image,
                   order_type_id AS orderTypeId,
                   start_price AS startPrice,
                   delivery_price AS deliveryPrice,
                   remark
            FROM business
            WHERE id = #{businessId}
            """)
    Business findById(@Param("businessId") Integer businessId);
}
