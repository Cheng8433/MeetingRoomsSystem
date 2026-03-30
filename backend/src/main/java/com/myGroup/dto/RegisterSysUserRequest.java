package com.myGroup.dto;
import lombok.Data;

@Data
public class RegisterSysUserRequest {
    private String userName;
    private String password;
    private String confirmPassword;
    private String phone;
    private String realName;
    private String email;
    private String department_name;
}
