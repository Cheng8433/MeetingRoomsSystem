package com.myGroup.WebConfig;

import org.springframework.web.bind.annotation.*;

@RestController
public class OptionsController {

    /**
     * 处理所有 OPTIONS 预检请求
     * 这个接口会自动处理浏览器的 CORS 预检
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public String handleOptions() {
        return "OK";
    }
}