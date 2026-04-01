package com.scau.dormrepair.domain.command;

import com.scau.dormrepair.domain.enums.UserRole;

public record AccountFilter(
        UserRole roleCode,
        Boolean enabled
) {

    public static AccountFilter all() {
        return new AccountFilter(null, null);
    }
}