package com.myGroup.controller;

import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
public class CaptchaController {

    @Autowired
    private Producer captchaProducer;

    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        // 设置响应类型
        response.setContentType("image/jpeg");

        // 生成验证码文本
        String capText = captchaProducer.createText();

        // 存入 session（注意：分布式环境需改为 Redis 存储）
        session.setAttribute("captcha", capText);

        // 生成图片
        BufferedImage bi = captchaProducer.createImage(capText);

        // 输出图片
        ImageIO.write(bi, "jpg", response.getOutputStream());
    }
}