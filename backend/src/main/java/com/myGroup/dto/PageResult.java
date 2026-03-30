package com.myGroup.dto;

import java.util.List;

public class PageResult<T> {
    @lombok.Getter
    @lombok.Setter
    private List<T> content;// 当前页数据

    @lombok.Getter
    @lombok.Setter
    private long totalElements;// 总记录数

    @lombok.Getter
    @lombok.Setter
    private int totalPages;            // 总页数

    @lombok.Getter
    @lombok.Setter
    private int currentPage;           // 当前页码（从0开始）

    @lombok.Getter
    @lombok.Setter
    private int pageSize;              // 每页大小

    // 构造器、getter/setter 省略（可用Lombok）
}