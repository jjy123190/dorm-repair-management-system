package com.scau.dormrepair.domain.entity;

import com.scau.dormrepair.domain.enums.UserRole;

/**
 * 用户账号实体。
 * 这张表统一容纳学生、管理员和维修人员三类账号。
 */
public class UserAccount extends BaseTimeEntity {

    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private String phone;
    private UserRole roleCode;
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(UserRole roleCode) {
        this.roleCode = roleCode;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
