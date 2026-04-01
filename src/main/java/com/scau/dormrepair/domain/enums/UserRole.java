package com.scau.dormrepair.domain.enums;

public enum UserRole {
    STUDENT("\u5b66\u751f"),
    ADMIN("\u5bbf\u7ba1\u7ba1\u7406\u5458"),
    WORKER("\u7ef4\u4fee\u5458");

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