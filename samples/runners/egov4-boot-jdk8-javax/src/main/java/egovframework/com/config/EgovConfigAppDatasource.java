package egovframework.com.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal in-memory datasource backed by HikariCP + HSQLDB (direct, no log4jdbc).
 *
 * <p>Mirrors the egov5-boot-jdk17-jakarta fix for HikariCP 6.x + HSQLDB 2.7.x +
 * log4jdbc ConnectionSpy incompatibility (HikariPool init aborts on
 * {@code Connection.setNetworkTimeout()} -&gt; SQLFeatureNotSupportedException).
 * HikariCP 4.x bundled with Spring Boot 2.7 swallows the exception, but
 * removing log4jdbc here keeps the two eGov boot runners symmetric.
 */
@Configuration
public class EgovConfigAppDatasource {

    @Bean(name = {"dataSource", "egov.dataSource", "egovDataSource"})
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        config.setJdbcUrl("jdbc:hsqldb:mem:nexacro;sql.syntax_mys=true");
        config.setUsername("sa");
        config.setPassword("");
        config.setMinimumIdle(3);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }
}
