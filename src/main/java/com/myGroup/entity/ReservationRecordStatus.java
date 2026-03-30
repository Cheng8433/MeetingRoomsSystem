package com.myGroup.entity;

public enum ReservationRecordStatus {
    PENDING("待审核"),       // 待审核（管理员审核）
    CONFIRMED("已确认"),     // 已确认/已批准
    IN_USE("使用中"),        // 使用中（进行时状态）
    COMPLETED("已完成"),     // 已完成
    CANCELLED("已取消"),     // 已取消
    REJECTED("已拒绝");      // 已拒绝（管理员拒绝）

    private final String description;

    ReservationRecordStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // 方便从字符串转换
    public static ReservationRecordStatus fromString(String status) {
        try {
            return ReservationRecordStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING; // 默认值
        }
    }
}
