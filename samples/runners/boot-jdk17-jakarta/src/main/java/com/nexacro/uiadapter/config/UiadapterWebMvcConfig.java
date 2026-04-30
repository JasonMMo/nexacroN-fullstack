package com.nexacro.uiadapter.config;

import com.nexacro.uiadapter.jakarta.core.resolve.NexacroMappingExceptionResolver;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroRequestMappingHandlerAdapter;
import com.nexacro.uiadapter.jakarta.dao.DbVendorsProvider;
import com.nexacro.uiadapter.jakarta.dao.Dbms;
import com.nexacro.uiadapter.jakarta.dao.dbms.Hsql;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wires the Nexacro uiadapter into Spring MVC.
 *
 * <p>By implementing {@link WebMvcRegistrations} and overriding
 * {@link #getRequestMappingHandlerAdapter()} we supply
 * {@link NexacroRequestMappingHandlerAdapter}, which in turn registers the
 * {@code @ParamDataSet} / {@code @ParamVariable} argument resolvers and the
 * {@link com.nexacro.uiadapter.jakarta.core.data.NexacroResult} return-value
 * handler. Without this bean controllers receive raw HTTP and the resolvers
 * never run.
 *
 * <p>Mirrors the canonical sample at
 * {@code https://gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta} —
 * see {@code example.nexacro.uiadapter.config.UiadapterWebMvcConfig}.
 */
@Configuration
public class UiadapterWebMvcConfig implements WebMvcConfigurer, WebMvcRegistrations {

    /** Returns the Nexacro adapter so Spring uses it instead of the default. */
    @Override
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new NexacroRequestMappingHandlerAdapter();
    }

    /**
     * Translates {@link com.nexacro.uiadapter.jakarta.core.NexacroException}
     * (and other handler errors) into a properly framed Nexacro envelope so
     * the client always sees a structured ErrorCode/ErrorMsg.
     */
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new NexacroMappingExceptionResolver());
    }

    /**
     * Registers DBMS metadata for HSQLDB so the MyBatis interceptors
     * ({@code NexacroMybatisMetaDataProvider} / {@code NexacroMybatisResultSetHandler})
     * can build column metadata for empty result sets.
     */
    @Bean
    public DbVendorsProvider dbmsProvider() {
        DbVendorsProvider provider = new DbVendorsProvider();
        Map<String, Dbms> vendors = new HashMap<>();
        // JDBC DatabaseMetaData#getDatabaseProductName() returns this exact string for HSQLDB.
        vendors.put("HSQL Database Engine", new Hsql());
        provider.setDbvendors(vendors);
        return provider;
    }
}
