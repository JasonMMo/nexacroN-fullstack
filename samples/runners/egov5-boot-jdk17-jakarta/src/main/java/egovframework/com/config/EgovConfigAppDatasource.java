package egovframework.com.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal in-memory datasource backed by HikariCP + HSQLDB (direct, no log4jdbc).
 *
 * <p>log4jdbc DriverSpy was removed because HikariCP 6.x (Spring Boot 3.x) calls
 * {@code Connection.setNetworkTimeout()} during connection setup. HSQLDB 2.7.x throws
 * {@code SQLFeatureNotSupportedException} for that method, and when wrapped in
 * log4jdbc's ConnectionSpy the exception propagates up through
 * {@code HikariPool.createPoolEntry()} and aborts pool initialization.
 * boot-jdk17-jakarta works because it uses the HSQLDB JDBC driver directly.
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
