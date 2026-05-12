package com.nexacro.uiadapter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.stereotype.Controller;

/**
 * Root application context: services, repositories, datasource, mybatis.
 * Excludes @Controller beans (handled by WebMvcConfig).
 */
@Configuration
@ComponentScan(
    basePackages = "com.nexacro.uiadapter",
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Controller.class)
)
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class RootConfig {
    // DataSource, MyBatis, and TransactionManager beans are in
    // DataSourceConfig and MyBatisConfig, which are picked up by @ComponentScan.
}
