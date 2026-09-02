package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Business;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商家数据访问接口。
 */
@Mapper
public interface BusinessMapper {
    /**
     * 查询商家列表，可按分类筛选。
     *
     * @param orderTypeId 分类编号
     * @return 商家列表
     */
    List<Business> list(@Param("orderTypeId") Integer orderTypeId);

    /**
     * 根据商家编号查询商家。
     *
     * @param businessId 商家编号
     * @return 商家信息
     */
    @Select("""
            SELECT id, name, address, description, image,
                   order_type_id, start_price, delivery_price, remark
            FROM business
            WHERE id = #{businessId}
            """)
    Business findById(@Param("businessId") Integer businessId);
}
