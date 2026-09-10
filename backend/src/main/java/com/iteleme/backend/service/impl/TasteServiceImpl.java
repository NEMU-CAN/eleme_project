package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.Taste;
import com.iteleme.backend.mapper.TasteMapper;
import com.iteleme.backend.service.TasteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TasteServiceImpl implements TasteService {
    private final TasteMapper tasteMapper;

    @Override
    public List<Taste> list() {
        return tasteMapper.list();
    }
}
