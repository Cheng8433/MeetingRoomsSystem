package com.myGroup.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MeetingRoom {
    private Integer roomId;
    private String roomName;
    private String roomNo;
    private Integer capacity;
    private BigDecimal area;
    private String description;
    private Integer roomStatus;
    private String photoUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer createUserId;

    // 关联的用户信息（可选，用于显示创建人信息）
    private SysUser createUser;

    // 无参构造函数
    public MeetingRoom() {
    }

    // 有参构造函数
    public MeetingRoom(Integer roomId, String roomName, String roomNo, Integer capacity,
                       BigDecimal area, String description, Integer roomStatus, String photoUrl,
                       LocalDateTime createTime, LocalDateTime updateTime, Integer createUserId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomNo = roomNo;
        this.capacity = capacity;
        this.area = area;
        this.description = description;
        this.roomStatus = roomStatus;
        this.photoUrl = photoUrl;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createUserId = createUserId;
    }

    public MeetingRoom(MeetingRoom meetingRoom) {
        this.roomId = meetingRoom.roomId;
        this.roomName =  meetingRoom.roomName;
        this.roomNo =   meetingRoom.roomNo;
        this.capacity =  meetingRoom.capacity;
        this.area =   meetingRoom.area;
        this.description =   meetingRoom.description;
        this.roomStatus =   meetingRoom.roomStatus;
        this.photoUrl =   meetingRoom.photoUrl;
        this.createTime = LocalDateTime.now();
        this.updateTime =  LocalDateTime.now();
        this.createUserId =  meetingRoom.createUserId;
    }

    // Getter和Setter方法
    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(Integer roomStatus) {
        this.roomStatus = roomStatus;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
    }

    public SysUser getCreateUser() {
        return createUser;
    }

    public void setCreateUser(SysUser createUser) {
        this.createUser = createUser;
    }

    @Override
    public String toString() {
        return "MeetingRoom{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", roomNo='" + roomNo + '\'' +
                ", capacity=" + capacity +
                ", area=" + area +
                ", description='" + description + '\'' +
                ", roomStatus=" + roomStatus +
                ", photoUrl='" + photoUrl + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createUserId=" + createUserId +
                '}';
    }
}
