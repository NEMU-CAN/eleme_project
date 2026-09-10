package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.Taste;

import java.util.List;

public interface TasteMapper {
    List<Taste> list();

    Taste findById(Integer id);
}
