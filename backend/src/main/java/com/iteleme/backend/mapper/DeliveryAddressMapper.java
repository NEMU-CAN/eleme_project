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

@Mapper
public interface DeliveryAddressMapper {
    @Select("""
            SELECT id,
                   contact_name AS contactName,
                   contact_sex AS contactSex,
                   contact_tel AS contactTel,
                   address,
                   user_id AS userId
            FROM deliveryaddress
            WHERE user_id = #{userId}
            ORDER BY id
            """)
    List<DeliveryAddress> findByUserId(@Param("userId") String userId);

    @Select("""
            SELECT id,
                   contact_name AS contactName,
                   contact_sex AS contactSex,
                   contact_tel AS contactTel,
                   address,
                   user_id AS userId
            FROM deliveryaddress
            WHERE user_id = #{userId} AND id = #{daId}
            """)
    DeliveryAddress findByIdForUser(@Param("userId") String userId, @Param("daId") Integer daId);

    @Insert("""
            INSERT INTO deliveryaddress(contact_name, contact_sex, contact_tel, address, user_id)
            VALUES (#{contactName}, #{contactSex}, #{contactTel}, #{address}, #{userId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(DeliveryAddress deliveryAddress);

    @Update("""
            UPDATE deliveryaddress
            SET contact_name = #{contactName},
                contact_sex = #{contactSex},
                contact_tel = #{contactTel},
                address = #{address}
            WHERE user_id = #{userId} AND id = #{id}
            """)
    int update(DeliveryAddress deliveryAddress);

    @Delete("DELETE FROM deliveryaddress WHERE user_id = #{userId} AND id = #{daId}")
    int deleteByIdForUser(@Param("userId") String userId, @Param("daId") Integer daId);
}
