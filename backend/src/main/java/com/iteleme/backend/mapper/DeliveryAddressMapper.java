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
            SELECT daId AS id, contactName, contactSex, contactTel, address, userId
            FROM deliveryaddress WHERE userId = #{userId} ORDER BY daId
            """)
    List<DeliveryAddress> findByUserId(@Param("userId") String userId);

    @Select("""
            SELECT daId AS id, contactName, contactSex, contactTel, address, userId
            FROM deliveryaddress WHERE userId = #{userId} AND daId = #{daId}
            """)
    DeliveryAddress findByIdForUser(@Param("userId") String userId, @Param("daId") Integer daId);

    @Insert("""
            INSERT INTO deliveryaddress(contactName, contactSex, contactTel, address, userId)
            VALUES (#{contactName}, #{contactSex}, #{contactTel}, #{address}, #{userId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "daId")
    int insert(DeliveryAddress deliveryAddress);

    @Update("""
            UPDATE deliveryaddress SET contactName=#{contactName}, contactSex=#{contactSex},
              contactTel=#{contactTel}, address=#{address}
            WHERE userId=#{userId} AND daId=#{id}
            """)
    int update(DeliveryAddress deliveryAddress);

    @Delete("DELETE FROM deliveryaddress WHERE userId=#{userId} AND daId=#{daId}")
    int deleteByIdForUser(@Param("userId") String userId, @Param("daId") Integer daId);
}
