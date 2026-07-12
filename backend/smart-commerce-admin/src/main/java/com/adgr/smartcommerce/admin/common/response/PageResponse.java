package com.adgr.smartcommerce.admin.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        long current,
        long size,
        long total,
        long pages,
        List<T> records) {

    public static <S, T> PageResponse<T> from(IPage<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                page.getRecords().stream().map(mapper).toList());
    }
}
