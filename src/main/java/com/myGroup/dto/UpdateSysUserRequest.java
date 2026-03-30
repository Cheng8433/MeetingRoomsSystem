package com.myGroup.dto;

import lombok.Data;

// 创建更新用户的DTO
@Data
public class UpdateSysUserRequest {
    private String name;
    private String email;
    private String department_name;

}
