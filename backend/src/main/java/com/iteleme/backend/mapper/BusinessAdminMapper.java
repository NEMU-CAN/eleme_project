package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.BusinessAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BusinessAdminMapper {
    int insert(BusinessAdmin businessAdmin);

    BusinessAdmin findByUserIdAndBusinessId(@Param("userId") Integer userId, @Param("businessId") Integer businessId);

    List<Integer> listBusinessIdsByUserId(@Param("userId") Integer userId);

    Integer nextId();
}
