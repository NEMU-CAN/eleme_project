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
    @Select("SELECT daId, contactName, contactSex, contactTel, address, userId FROM deliveryaddress WHERE userId = #{userId} ORDER BY daId")
    List<DeliveryAddress> findByUserId(@Param("userId") String userId);

    @Select("SELECT daId, contactName, contactSex, contactTel, address, userId FROM deliveryaddress WHERE daId = #{daId}")
    DeliveryAddress findById(@Param("daId") Integer daId);

    @Insert("INSERT INTO deliveryaddress (contactName, contactSex, contactTel, address, userId) VALUES (#{contactName}, #{contactSex}, #{contactTel}, #{address}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "daId", keyColumn = "daId")
    int insert(DeliveryAddress address);

    @Update("UPDATE deliveryaddress SET contactName=#{contactName}, contactSex=#{contactSex}, contactTel=#{contactTel}, address=#{address} WHERE daId=#{daId}")
    int update(DeliveryAddress address);

    @Delete("DELETE FROM deliveryaddress WHERE daId = #{daId}")
    int deleteById(@Param("daId") Integer daId);
}
