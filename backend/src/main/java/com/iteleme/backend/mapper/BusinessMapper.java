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
            SELECT businessId AS id, businessName AS name, businessAddress AS address,
                   businessExplain AS description, businessImg AS image,
                   orderTypeId, starPrice AS startPrice, deliveryPrice, remarks AS remark
            FROM business
            WHERE businessId = #{businessId}
            """)
    Business findById(@Param("businessId") Integer businessId);
}
