package com.myGroup.WebConfig;

import org.springframework.web.bind.annotation.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")  // 添加这个注解
public class TestController {

    /**
     * 测试连接 - GET 方法
     */
    @GetMapping("/network-info")
    public Map<String, Object> getNetworkInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            InetAddress ip = InetAddress.getLocalHost();
            result.put("status", "success");
            result.put("hostName", ip.getHostName());
            result.put("ipAddress", ip.getHostAddress());
            result.put("localUrl", "http://localhost:8080");
            result.put("networkUrl", "http://" + ip.getHostAddress() + ":8080");
            result.put("timestamp", System.currentTimeMillis());
            result.put("message", "后端服务正常运行");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 简单的健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "GroupWorkApplication");
        result.put("port", 8080);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 通用接口 - 返回简单消息
     */
    @GetMapping("/api/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Hello from IDEA backend!");
        result.put("data", "This is a test message");
        return result;
    }
}
