package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Business;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BusinessMapper {
    @Select("""
            <script>
            SELECT businessId, businessName, businessAddress, businessExplain, businessImg,
                   orderTypeId, starPrice, deliveryPrice, remarks
            FROM business
            <where>
                <if test="orderTypeId != null">
                    orderTypeId = #{orderTypeId}
                </if>
            </where>
            ORDER BY businessId
            </script>
            """)
    List<Business> list(@Param("orderTypeId") Integer orderTypeId);

    @Select("""
            SELECT businessId, businessName, businessAddress, businessExplain, businessImg,
                   orderTypeId, starPrice, deliveryPrice, remarks
            FROM business
            WHERE businessId = #{businessId}
            """)
    Business findById(@Param("businessId") Integer businessId);
}
