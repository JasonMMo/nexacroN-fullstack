package com.nexacro.uiadapter.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DataSourceConfig {

    @Value("${datasource.url:jdbc:hsqldb:mem:nexacro;sql.syntax_mys=true}")
    private String url;

    @Value("${datasource.username:sa}")
    private String username;

    @Value("${datasource.password:}")
    private String password;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSeparator("^^");
        populator.setContinueOnError(false);
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        DataSourceInitializer init = new DataSourceInitializer();
        init.setDataSource(dataSource);
        init.setDatabasePopulator(populator);
        return init;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
