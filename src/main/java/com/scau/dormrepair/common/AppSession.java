package com.scau.dormrepair.common;

import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppSession {

    private final BooleanProperty authenticated = new SimpleBooleanProperty(false);
    private final StringProperty displayName = new SimpleStringProperty("\u672a\u767b\u5f55");
    private final StringProperty username = new SimpleStringProperty("");
    private final ObjectProperty<UserRole> currentRole = new SimpleObjectProperty<>();
    private final ObjectProperty<Long> currentAccountId = new SimpleObjectProperty<>();

    public void login(UserAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("\u767b\u5f55\u8d26\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
        }
        displayName.set(account.getDisplayName());
        username.set(account.getUsername() == null ? "" : account.getUsername());
        currentRole.set(account.getRoleCode());
        currentAccountId.set(account.getId());
        authenticated.set(true);
    }

    public void logout() {
        displayName.set("\u672a\u767b\u5f55");
        username.set("");
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

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
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