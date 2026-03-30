package com.myGroup.entity;

import lombok.Getter;

@Getter
public enum SysUserRole {
    admin("admin"),
    user("user");
    private final String value;

    SysUserRole(String value) {
        this.value = value;
    }

    // 从字符串转换为枚举
    public static SysUserRole fromValue(String value) {
        for (SysUserRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色: " + value);
    }
}