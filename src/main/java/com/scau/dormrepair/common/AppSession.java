package com.scau.dormrepair.common;

import com.scau.dormrepair.domain.enums.UserRole;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 桌面端登录态与角色上下文。
 */
public class AppSession {

    private final BooleanProperty authenticated = new SimpleBooleanProperty(false);
    private final StringProperty displayName = new SimpleStringProperty("未登录");
    private final ObjectProperty<UserRole> currentRole = new SimpleObjectProperty<>();
    private final ObjectProperty<Long> currentAccountId = new SimpleObjectProperty<>();

    public void login(String name, UserRole role) {
        displayName.set(name == null || name.isBlank() ? "未命名用户" : name.trim());
        currentRole.set(role);
        currentAccountId.set(null);
        authenticated.set(true);
    }

    /**
     * 正式项目阶段改为使用稳定账号登录。
     * 这样页面不再依赖“手填名字”，后续接真实用户表时也只需要把账号来源替换掉。
     */
    public void login(DemoAccountDirectory.DemoAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("登录账号不能为空");
        }
        displayName.set(account.displayName());
        currentRole.set(account.role());
        currentAccountId.set(account.id());
        authenticated.set(true);
    }

    public void logout() {
        displayName.set("未登录");
        authenticated.set(false);
        currentRole.set(null);
        currentAccountId.set(null);
    }

    public boolean isAuthenticated() {
        return authenticated.get();
    }

    public BooleanProperty authenticatedProperty() {
        return authenticated;
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public StringProperty displayNameProperty() {
        return displayName;
    }

    public UserRole getCurrentRole() {
        return currentRole.get();
    }

    public ObjectProperty<UserRole> currentRoleProperty() {
        return currentRole;
    }

    public Long getCurrentAccountId() {
        return currentAccountId.get();
    }

    public ObjectProperty<Long> currentAccountIdProperty() {
        return currentAccountId;
    }
}
