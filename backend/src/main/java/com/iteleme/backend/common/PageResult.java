package com.iteleme.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private int page;
    private int pageSize;
    private List<T> records;

    public static <T> PageResult<T> of(long total, int page, int pageSize, List<T> records) {
        return new PageResult<>(total, page, pageSize, records);
    }
}
