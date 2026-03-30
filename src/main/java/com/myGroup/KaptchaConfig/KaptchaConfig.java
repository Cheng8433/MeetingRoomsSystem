package com.myGroup.KaptchaConfig;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 * 用于配置 Kaptcha 图片验证码的样式、长度、干扰线等属性
 */
@Configuration
public class KaptchaConfig {

    /**
     * 配置 Kaptcha 的 Producer Bean，用于生成验证码文本和图片
     */
    @Bean
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();

        // 定义配置属性
        Properties properties = new Properties();

        // 1. 验证码图片宽度（像素）
        properties.setProperty("kaptcha.image.width", "150");

        // 2. 验证码图片高度（像素）
        properties.setProperty("kaptcha.image.height", "50");

        // 3. 验证码字符集（生成的字符来源）
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ");
        // 注意：去掉了容易混淆的字符 O,0,I,1 等

        // 4. 验证码字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");

        // 5. 验证码字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "40");

        // 6. 验证码字体颜色（RGB格式）
        properties.setProperty("kaptcha.textproducer.font.color", "black");

        // 7. 验证码字体名称（可以设置多个，用逗号分隔）
        properties.setProperty("kaptcha.textproducer.font.names", "Arial, Courier");

        // 8. 图片边框（有效边框，合法值：yes, no）
        properties.setProperty("kaptcha.border", "yes");

        // 9. 边框颜色
        properties.setProperty("kaptcha.border.color", "105,179,90");

        // 10. 背景颜色（从渐变色起始颜色）
        properties.setProperty("kaptcha.background.clear.from", "255,255,255");

        // 11. 背景颜色（渐变色结束颜色）
        properties.setProperty("kaptcha.background.clear.to", "255,255,255");

        // 12. 干扰线颜色
        properties.setProperty("kaptcha.noise.color", "blue");

        // 13. 干扰线实现类（默认提供了DefaultNoise，也可以自定义）
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");

        // 14. 文本生产者实现类（默认提供了DefaultTextProducer，也可以自定义）
        //properties.setProperty("kaptcha.textproducer.impl", "com.google.code.kaptcha.text.impl.DefaultTextProducer");

        // 15. 图片样式（水纹：WaterRipple，鱼眼：FishEyeGimpy，阴影：ShadowGimpy 等）
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");

        // 将属性设置到 Config 对象中
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);

        return defaultKaptcha;
    }
}