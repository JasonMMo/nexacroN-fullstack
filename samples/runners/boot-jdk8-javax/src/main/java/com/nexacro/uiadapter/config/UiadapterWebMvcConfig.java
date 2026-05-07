package com.nexacro.uiadapter.config;

import com.nexacro.uiadapter.spring.core.resolve.NexacroMappingExceptionResolver;
import com.nexacro.uiadapter.spring.core.resolve.NexacroMethodArgumentResolver;
import com.nexacro.uiadapter.spring.core.resolve.NexacroRequestMappingHandlerAdapter;
import com.nexacro.uiadapter.spring.dao.DbVendorsProvider;
import com.nexacro.uiadapter.spring.dao.Dbms;
import com.nexacro.uiadapter.spring.dao.dbms.Hsql;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wires the Nexacro uiadapter into Spring MVC (javax lane / Boot 2.7 / Spring 5.3).
 *
 * <p>Mirrors {@code com.nexacro.uiadapter.config.UiadapterWebMvcConfig} from the
 * jakarta lane; the only differences are the {@code spring.core} / {@code spring.dao}
 * package paths shipped by {@code uiadapter-spring-core} and {@code uiadapter-spring-dataaccess}.
 */
@Configuration
public class UiadapterWebMvcConfig implements WebMvcConfigurer, WebMvcRegistrations {

    @Override
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new NexacroRequestMappingHandlerAdapter();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new NexacroMethodArgumentResolver());
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new NexacroMappingExceptionResolver());
    }

    @Bean
    public DbVendorsProvider dbmsProvider() {
        DbVendorsProvider provider = new DbVendorsProvider();
        Map<String, Dbms> vendors = new HashMap<>();
        vendors.put("HSQL Database Engine", new Hsql());
        provider.setDbvendors(vendors);
        return provider;
    }
}
