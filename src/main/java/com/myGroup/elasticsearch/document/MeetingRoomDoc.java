package com.myGroup.elasticsearch.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "meeting_rooms")  // 索引名称，可自定义
public class MeetingRoomDoc {

    @Id
    private Integer roomId;  // 对应 MySQL 的主键

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    private String roomName;  // 会议室名称，全文搜索

    @Field(type = FieldType.Keyword)
    private String roomNo;    // 房间号，精确匹配

    @Field(type = FieldType.Integer)
    private Integer capacity; // 容量，范围查询

    @Field(type = FieldType.Double)
    private BigDecimal area;  // 面积，也可作为价格字段使用

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description; // 描述，全文搜索

    @Field(type = FieldType.Integer)
    private Integer roomStatus; // 状态（1可用/0不可用）

    @Field(type = FieldType.Keyword, index = false)
    private String photoUrl;    // 图片 URL，不参与搜索

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createTime;


    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updateTime;

    @Field(type = FieldType.Integer)
    private Integer createUserId;

    // 无参构造
    public MeetingRoomDoc() {}

    // Getter & Setter（请手动生成或使用 Lombok）
    public  Integer getRoomId() {
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

}