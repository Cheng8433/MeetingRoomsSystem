// 文件位置：src/main/java/com/example/exception/BusinessException.java
package com.myGroup.exception;

/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException {
    private String code;

    public BusinessException(String message) {
        super(message);
        this.code = "400";
    }

    public BusinessException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
