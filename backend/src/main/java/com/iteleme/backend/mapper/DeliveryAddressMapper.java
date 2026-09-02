package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.DeliveryAddress;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 送货地址数据访问接口。
 */
@Mapper
public interface DeliveryAddressMapper {
    /**
     * 查询某个用户的地址列表。
     */
    @Select("""
            SELECT id, contact_name, contact_sex, contact_tel, address, user_id
            FROM deliveryaddress
            WHERE user_id = #{userId}
            ORDER BY id
            """)
    List<DeliveryAddress> findByUserId(@Param("userId") String userId);

    /**
     * 根据用户和地址编号查询单条地址。
     */
    @Select("""
            SELECT id, contact_name, contact_sex, contact_tel, address, user_id
            FROM deliveryaddress
            WHERE user_id = #{userId}
              AND id = #{daId}
            """)
    DeliveryAddress findByIdForUser(@Param("userId") String userId, @Param("daId") Integer daId);

    /**
     * 新增送货地址。
     */
    @Insert("""
            INSERT INTO deliveryaddress(contact_name, contact_sex, contact_tel, address, user_id)
            VALUES (#{contactName}, #{contactSex}, #{contactTel}, #{address}, #{userId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DeliveryAddress deliveryAddress);

    /**
     * 更新送货地址。
     */
    @Update("""
            UPDATE deliveryaddress
            SET contact_name = #{contactName},
                contact_sex = #{contactSex},
                contact_tel = #{contactTel},
                address = #{address}
            WHERE user_id = #{userId}
              AND id = #{id}
            """)
    int update(DeliveryAddress deliveryAddress);

    /**
     * 删除指定地址。
     */
    @Delete("""
            DELETE FROM deliveryaddress
            WHERE user_id = #{userId}
              AND id = #{daId}
            """)
    int deleteByIdForUser(@Param("userId") String userId, @Param("daId") Integer daId);
}
