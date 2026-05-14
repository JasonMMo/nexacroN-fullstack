package egovframework.com.config;

import java.util.HashMap;
import java.util.Map;

import egovframework.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Bridges legacy eGovFrame property lookups onto Spring's Environment.
 *
 * <p>Uses the eGov 4 lineage (legacy 3.10.0 RTE) {@code egovframework.rte.fdl.property}
 * package. The newer eGov 5 {@code org.egovframe.rte.fdl.property} package is not
 * applicable here because no eGov 4 boot-starter-parent is published.
 */
@Configuration
@PropertySource("classpath:globals.properties")
public class EgovConfigAppProperties {

    private final Environment env;

    public EgovConfigAppProperties(Environment env) {
        this.env = env;
    }

    @Bean(destroyMethod = "destroy")
    public EgovPropertyServiceImpl propertiesService() {
        Map<String, String> properties = new HashMap<>();
        copy(properties, "Globals.OsType");
        copy(properties, "Globals.DbType");
        copy(properties, "Globals.MainPage");
        copy(properties, "Globals.fileStorePath");

        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        service.setProperties(properties);
        return service;
    }

    private void copy(Map<String, String> properties, String key) {
        String value = env.getProperty(key);
        if (value != null) {
            properties.put(key, value);
        }
    }
}
