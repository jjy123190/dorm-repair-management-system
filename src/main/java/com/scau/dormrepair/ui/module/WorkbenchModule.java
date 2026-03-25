package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.domain.enums.UserRole;
import java.util.EnumSet;
import java.util.Set;
import javafx.scene.Parent;

/**
 * 所有工作台模块统一遵守的接口。
 */
public interface WorkbenchModule {

    String moduleCode();

    String moduleName();

    String moduleDescription();

    default Set<UserRole> supportedRoles() {
        return EnumSet.allOf(UserRole.class);
    }

    default boolean supports(UserRole role) {
        return role != null && supportedRoles().contains(role);
    }

    /**
     * 默认每次切回模块都重建页面，优先保证最新数据。
     * 如果某个模块更看重切换手感，可以显式返回 true 保留页面实例。
     */
    default boolean cacheViewOnSwitch() {
        return false;
    }

    Parent createView();
}
