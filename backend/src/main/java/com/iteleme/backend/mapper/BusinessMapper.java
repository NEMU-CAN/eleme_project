package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Business;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BusinessMapper {
    List<Business> list(@Param("tasteId") Integer tasteId,
                        @Param("status") Integer status,
                        @Param("keyword") String keyword);

    Business findById(@Param("id") Integer id);

    int insert(Business business);

    int update(Business business);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    Integer nextId();
}
