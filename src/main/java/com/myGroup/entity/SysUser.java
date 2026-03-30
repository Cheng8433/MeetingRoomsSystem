package com.myGroup.entity;
import lombok.Setter;

import java.time.LocalDateTime;

public class SysUser {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private int status;
    private SysUserRole role;
    private String phone;
    private LocalDateTime createTime;// 对应TIMESTAMP类型
    private LocalDateTime updateTime;// 对应TIMESTAMP类型
    @Setter
    private String department_name;

    // 无参构造方法
    public SysUser() {
    }

    // 全参构造方法
    public SysUser(Integer id, String username, String password,String email, SysUserRole role, LocalDateTime createTime, String department_name) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.createTime = createTime;
        this.department_name = department_name;
    }

    // 部分参数构造方法（常用字段）
    public SysUser(String username, String email, SysUserRole role, String department_name) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.department_name = department_name;
        this.createTime = LocalDateTime.now(); // 默认设置为当前时间
    }

    // getter和setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSysUsername() { return username; }
    public void setSysUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int  getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public SysUserRole getRole() { return role; }
    public void setRole(SysUserRole role) { this.role = role; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTimeToNow() {
        this.createTime = LocalDateTime.now();
    }

    public String getDepartment_name() { return department_name; }

    public String  getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    // toString方法
    @Override
    public String toString() {
        return "SysUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status + '\'' +
                ", role=" + role + '\'' +
                ", phone=" + phone + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", department_name='" + department_name + '\'' +
                '}';
    }
}