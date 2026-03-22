package com.scau.dormrepair.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 前端专用的分页返回结构。
 * 不直接暴露 Spring Page，避免前端绑定 Spring 的内部字段结构。
 */
public record PageResponse<T>(
        List<T> list,
        int page,
        int size,
        long total,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> pageData) {
        // 统一把 Spring 的 0 基页码转换为前端常用的 1 基页码。
        return new PageResponse<>(
                pageData.getContent(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
