package com.myGroup.entity;

import java.time.LocalDateTime;

public class SysNotice {
    private Integer noticeId;
    private Integer receiveUserId;
    private Integer noticeType;
    private Integer relatedId;
    private String noticeContent;
    private Integer readStatus;
    private LocalDateTime createTime;

    // 关联信息
    private SysUser receiveUser; // 接收用户信息

    // 无参构造函数
    public SysNotice() {
    }

    // 有参构造函数
    public SysNotice(Integer noticeId, Integer receiveUserId, Integer noticeType,
                     Integer relatedId, String noticeContent, Integer readStatus,
                     LocalDateTime createTime) {
        this.noticeId = noticeId;
        this.receiveUserId = receiveUserId;
        this.noticeType = noticeType;
        this.relatedId = relatedId;
        this.noticeContent = noticeContent;
        this.readStatus = readStatus;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    public Integer getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Integer noticeId) {
        this.noticeId = noticeId;
    }

    public Integer getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(Integer receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public Integer getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(Integer noticeType) {
        this.noticeType = noticeType;
    }

    public Integer getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Integer relatedId) {
        this.relatedId = relatedId;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public Integer getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(Integer readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public SysUser getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(SysUser receiveUser) {
        this.receiveUser = receiveUser;
    }

    @Override
    public String toString() {
        return "SysNotice{" +
                "noticeId=" + noticeId +
                ", receiveUserId=" + receiveUserId +
                ", noticeType=" + noticeType +
                ", relatedId=" + relatedId +
                ", noticeContent='" + noticeContent + '\'' +
                ", readStatus=" + readStatus +
                ", createTime=" + createTime +
                '}';
    }
}