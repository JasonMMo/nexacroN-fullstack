package com.nexacro.uiadapter.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.java.xeni.services.GridExportImportServlet;
import com.nexacro.uiadapter.spring.core.context.ApplicationContextProvider;
import com.nexacro.uiadapter.spring.core.resolve.NexacroHandlerMethodReturnValueHandler;
import com.nexacro.uiadapter.spring.core.resolve.NexacroMappingExceptionResolver;
import com.nexacro.uiadapter.spring.core.resolve.NexacroMethodArgumentResolver;
import com.nexacro.uiadapter.spring.core.resolve.NexacroRequestMappingHandlerAdapter;
import com.nexacro.uiadapter.spring.core.view.NexacroFileView;
import com.nexacro.uiadapter.spring.core.view.NexacroView;
import com.nexacro.uiadapter.spring.dao.DbVendorsProvider;
import com.nexacro.uiadapter.spring.dao.Dbms;
import com.nexacro.uiadapter.spring.dao.dbms.Hsql;

/**
 * Wires Nexacro xapi / xeni / uiadapter modules into Spring MVC (javax lane / Boot 2.7).
 *
 * <p>Mirrors {@code com.nexacro.uiadapter.config.UiadapterWebMvcConfig} from the
 * jakarta lane (canonical sample at
 * {@code https://gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta}).
 * The only differences are the {@code spring.core} / {@code spring.dao} package paths
 * shipped by {@code uiadapter-spring-core} and {@code uiadapter-spring-dataaccess}, and
 * the {@code javax.servlet.*} imports.
 */
@Configuration
public class UiadapterWebMvcConfig implements WebMvcConfigurer, WebMvcRegistrations {

    private static final Logger logger = LoggerFactory.getLogger(UiadapterWebMvcConfig.class);

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public MultipartFilter multipartFilter() {
        MultipartFilter multipartFilter = new MultipartFilter();
        multipartFilter.setMultipartResolverBeanName("multipartResolver");
        return multipartFilter;
    }

    @Bean
    public ServletRegistrationBean<GridExportImportServlet> gridExportImportServletBean() {
        ServletRegistrationBean<GridExportImportServlet> bean =
                new ServletRegistrationBean<GridExportImportServlet>(
                        new GridExportImportServlet(), "/XExportImport.do");
        bean.setLoadOnStartup(1);
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                System.getProperty("java.io.tmpdir"),
                10000000,
                10000000 * 2,
                10000000 / 2);
        bean.setMultipartConfig(multipartConfigElement);
        logger.debug("register GridExportImportServlet at /XExportImport.do");
        return bean;
    }

    @Override
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new NexacroRequestMappingHandlerAdapter();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new NexacroMethodArgumentResolver());
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        NexacroHandlerMethodReturnValueHandler returnValueHandler =
                new NexacroHandlerMethodReturnValueHandler();

        NexacroView nexacroView = new NexacroView();
        nexacroView.setDefaultContentType(PlatformType.CONTENT_TYPE_XML);
        nexacroView.setDefaultCharset("UTF-8");

        returnValueHandler.setView(nexacroView);
        returnValueHandler.setFileView(new NexacroFileView());

        handlers.add(returnValueHandler);
        WebMvcConfigurer.super.addReturnValueHandlers(handlers);
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        NexacroView nexacroView = new NexacroView();
        nexacroView.setDefaultContentType(PlatformType.CONTENT_TYPE_XML);
        nexacroView.setDefaultCharset("UTF-8");

        NexacroMappingExceptionResolver nexacroException = new NexacroMappingExceptionResolver();
        nexacroException.setView(nexacroView);
        nexacroException.setShouldLogStackTrace(true);
        nexacroException.setShouldSendStackTrace(true);
        nexacroException.setDefaultErrorMsg("fail.common.msg");
        nexacroException.setOrder(1);
        resolvers.add(nexacroException);

        WebMvcConfigurer.super.configureHandlerExceptionResolvers(resolvers);
    }

    @Bean
    public ServletContextInitializer xeniExcelInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.setInitParameter("export-path",        "/excel");
                servletContext.setInitParameter("import-path",        "/excel");
                servletContext.setInitParameter("monitor-enabled",    "true");
                servletContext.setInitParameter("monitor-cycle-time", "30");
                servletContext.setInitParameter("file-storage-time",  "10");
            }
        };
    }

    @Bean
    public DbVendorsProvider dbmsProvider() {
        DbVendorsProvider provider = new DbVendorsProvider();
        Map<String, Dbms> vendors = new HashMap<String, Dbms>();
        vendors.put("HSQL Database Engine", new Hsql());
        provider.setDbvendors(vendors);
        return provider;
    }
}
