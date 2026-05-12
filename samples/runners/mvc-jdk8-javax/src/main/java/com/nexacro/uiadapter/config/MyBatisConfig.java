package com.nexacro.uiadapter.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * MyBatis configuration: SqlSessionFactory + MapperScannerConfigurer.
 */
@Configuration
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        fb.setConfigLocation(resolver.getResource("classpath:mybatis/sql-mapper-config.xml"));
        fb.setMapperLocations(resolver.getResources("classpath:mybatis/mappers/*-mapper.xml"));
        fb.setTypeAliasesPackage("com.nexacro.uiadapter.domain");
        return fb.getObject();
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer msc = new MapperScannerConfigurer();
        msc.setBasePackage("com.nexacro.uiadapter.mapper");
        msc.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return msc;
    }
}
