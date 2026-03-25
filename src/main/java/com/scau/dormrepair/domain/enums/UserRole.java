package com.scau.dormrepair.domain.enums;

/**
 * 用户角色枚举。
 */
public enum UserRole {
    STUDENT("学生"),
    ADMIN("宿管管理员"),
    WORKER("维修员");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
