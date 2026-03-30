package com.myGroup.entity;

import java.time.LocalDateTime;

public class SysDict {
    private Integer dictId;
    private String dictType;
    private String dictCode;
    private String dictName;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;

    // 无参构造函数
    public SysDict() {
    }

    // 有参构造函数
    public SysDict(Integer dictId, String dictType, String dictCode,
                   String dictName, Integer sort, Integer status, LocalDateTime createTime) {
        this.dictId = dictId;
        this.dictType = dictType;
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.sort = sort;
        this.status = status;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    public Integer getDictId() {
        return dictId;
    }

    public void setDictId(Integer dictId) {
        this.dictId = dictId;
    }

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SysDict{" +
                "dictId=" + dictId +
                ", dictType='" + dictType + '\'' +
                ", dictCode='" + dictCode + '\'' +
                ", dictName='" + dictName + '\'' +
                ", sort=" + sort +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
