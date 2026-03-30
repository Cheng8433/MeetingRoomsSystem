package com.myGroup.dto;

public class LoginRequest {
    private String username;
    private String password;
    private String captcha; // 新增：用户输入的验证码

    public LoginRequest(){}

    // getters and setters...
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getCaptcha() {
        return captcha;
    }
    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

}