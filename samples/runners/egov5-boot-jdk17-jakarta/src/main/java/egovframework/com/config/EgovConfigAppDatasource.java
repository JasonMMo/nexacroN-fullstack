package egovframework.com.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal in-memory datasource backed by log4jdbc + HikariCP.
 */
@Configuration
public class EgovConfigAppDatasource {

    @Bean(name = {"dataSource", "egov.dataSource", "egovDataSource"})
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
        config.setJdbcUrl("jdbc:log4jdbc:hsqldb:mem:nexacro;sql.syntax_mys=true");
        config.setUsername("sa");
        config.setPassword("");
        config.setMinimumIdle(3);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }
}
