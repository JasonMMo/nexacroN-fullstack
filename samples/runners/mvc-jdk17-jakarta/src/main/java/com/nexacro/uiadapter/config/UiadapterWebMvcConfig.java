package com.nexacro.uiadapter.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.uiadapter.jakarta.core.context.ApplicationContextProvider;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroHandlerMethodReturnValueHandler;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroMappingExceptionResolver;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroMethodArgumentResolver;
import com.nexacro.uiadapter.jakarta.core.view.NexacroFileView;
import com.nexacro.uiadapter.jakarta.core.view.NexacroStreamView;
import com.nexacro.uiadapter.jakarta.core.view.NexacroView;
import com.nexacro.uiadapter.jakarta.dao.DbVendorsProvider;
import com.nexacro.uiadapter.jakarta.dao.Dbms;
import com.nexacro.uiadapter.jakarta.dao.dbms.Hsql;

@Configuration
public class UiadapterWebMvcConfig implements WebMvcConfigurer {

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new NexacroMethodArgumentResolver());
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        NexacroHandlerMethodReturnValueHandler h = new NexacroHandlerMethodReturnValueHandler();
        NexacroView v = new NexacroView();
        v.setDefaultContentType(PlatformType.CONTENT_TYPE_XML);
        v.setDefaultCharset("UTF-8");
        h.setView(v);
        h.setFileView(new NexacroFileView());
        h.setStreamView(new NexacroStreamView());
        handlers.add(h);
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        NexacroView v = new NexacroView();
        v.setDefaultContentType(PlatformType.CONTENT_TYPE_XML);
        v.setDefaultCharset("UTF-8");
        NexacroMappingExceptionResolver r = new NexacroMappingExceptionResolver();
        r.setView(v);
        r.setShouldLogStackTrace(true);
        r.setShouldSendStackTrace(true);
        r.setDefaultErrorMsg("fail.common.msg");
        r.setOrder(1);
        resolvers.add(r);
    }

    @Bean
    public DbVendorsProvider dbmsProvider() {
        DbVendorsProvider p = new DbVendorsProvider();
        Map<String,Dbms> vendors = new HashMap<>();
        vendors.put("HSQL Database Engine", new Hsql());
        p.setDbvendors(vendors);
        return p;
    }
}
