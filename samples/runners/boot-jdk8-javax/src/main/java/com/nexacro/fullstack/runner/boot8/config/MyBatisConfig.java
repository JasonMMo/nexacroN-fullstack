package com.nexacro.fullstack.runner.boot8.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.nexacro.fullstack.business.domain")
public class MyBatisConfig {
}
