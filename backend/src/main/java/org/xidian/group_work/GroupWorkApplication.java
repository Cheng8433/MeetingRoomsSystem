package org.xidian.group_work;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@MapperScan("com.myGroup.mapper")  // 添加这行
@ComponentScan(basePackages = {"com.myGroup.controller", "com.myGroup.service", "com.myGroup.exception","com.myGroup.WebConfig","com.myGroup.jedis","com.myGroup.elasticsearch","com.myGroup.KaptchaConfig"})
@EnableElasticsearchRepositories(basePackages = "com.myGroup.elasticsearch.repository")
public class GroupWorkApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupWorkApplication.class, args);
    }

}
