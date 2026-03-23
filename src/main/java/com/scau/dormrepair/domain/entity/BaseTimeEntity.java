package com.scau.dormrepair.domain.entity;

import java.time.LocalDateTime;

/**
 * 统一抽取创建时间和更新时间字段。
 * 现在时间字段改为普通 POJO 属性，由 MyBatis 映射数据库中的审计列。
 */
public abstract class BaseTimeEntity {

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
