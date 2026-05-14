package egovframework.com.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Enables Spring-managed transactions for the Nexacro data layer.
 */
@Configuration
@EnableTransactionManagement
public class EgovConfigAppTransaction {

    private final DataSource dataSource;

    public EgovConfigAppTransaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
