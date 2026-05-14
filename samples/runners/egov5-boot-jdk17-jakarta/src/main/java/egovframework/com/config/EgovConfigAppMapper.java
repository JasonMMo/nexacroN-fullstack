package egovframework.com.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * MyBatis integration aligned with the canonical Nexacro mapper layout.
 */
@Configuration
@MapperScan(basePackages = "com.nexacro.uiadapter.mapper")
public class EgovConfigAppMapper {

    private static final String CONFIG_LOCATION = "classpath:mybatis/sql-mapper-config.xml";
    private static final String MAPPER_LOCATIONS = "classpath:mybatis/mappers/*-mapper.xml";

    private final DataSource dataSource;

    public EgovConfigAppMapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setConfigLocation(resolver.getResource(CONFIG_LOCATION));
        factoryBean.setMapperLocations(resolver.getResources(MAPPER_LOCATIONS));
        factoryBean.setTypeAliasesPackage("com.nexacro.uiadapter.domain");

        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
