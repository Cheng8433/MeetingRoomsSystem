package com.myGroup.entity;

import java.time.LocalDateTime;

public class SysOperLog {
    private Integer logId;
    private Integer operUserId;
    private String operUserName;
    private String operModule;
    private String operType;
    private String operContent;
    private String operIp;
    private LocalDateTime operTime;

    // 无参构造函数
    public SysOperLog() {
    }

    // 有参构造函数
    public SysOperLog(Integer logId, Integer operUserId, String operUserName,
                      String operModule, String operType, String operContent,
                      String operIp, LocalDateTime operTime) {
        this.logId = logId;
        this.operUserId = operUserId;
        this.operUserName = operUserName;
        this.operModule = operModule;
        this.operType = operType;
        this.operContent = operContent;
        this.operIp = operIp;
        this.operTime = operTime;
    }

    // Getter和Setter方法
    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getOperUserId() {
        return operUserId;
    }

    public void setOperUserId(Integer operUserId) {
        this.operUserId = operUserId;
    }

    public String getOperUserName() {
        return operUserName;
    }

    public void setOperUserName(String operUserName) {
        this.operUserName = operUserName;
    }

    public String getOperModule() {
        return operModule;
    }

    public void setOperModule(String operModule) {
        this.operModule = operModule;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public String getOperContent() {
        return operContent;
    }

    public void setOperContent(String operContent) {
        this.operContent = operContent;
    }

    public String getOperIp() {
        return operIp;
    }

    public void setOperIp(String operIp) {
        this.operIp = operIp;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public void setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
    }

    @Override
    public String toString() {
        return "SysOperLog{" +
                "logId=" + logId +
                ", operUserId=" + operUserId +
                ", operUserName='" + operUserName + '\'' +
                ", operModule='" + operModule + '\'' +
                ", operType='" + operType + '\'' +
                ", operContent='" + operContent + '\'' +
                ", operIp='" + operIp + '\'' +
                ", operTime=" + operTime +
                '}';
    }
}