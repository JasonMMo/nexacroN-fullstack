package com.nexacro.uiadapter.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

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
import com.nexacro.uiadapter.jakarta.core.context.ApplicationContextProvider;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroHandlerMethodReturnValueHandler;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroMappingExceptionResolver;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroMethodArgumentResolver;
import com.nexacro.uiadapter.jakarta.core.resolve.NexacroRequestMappingHandlerAdapter;
import com.nexacro.uiadapter.jakarta.core.view.NexacroFileView;
import com.nexacro.uiadapter.jakarta.core.view.NexacroStreamView;
import com.nexacro.uiadapter.jakarta.core.view.NexacroView;
import com.nexacro.uiadapter.jakarta.dao.DbVendorsProvider;
import com.nexacro.uiadapter.jakarta.dao.Dbms;
import com.nexacro.uiadapter.jakarta.dao.dbms.Hsql;

/**
 * Wires Nexacro xapi / xeni / uiadapter modules into Spring MVC.
 *
 * <p>Mirrors the canonical sample at
 * {@code https://gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta} —
 * see {@code example.nexacro.uiadapter.config.UiadapterWebMvcConfig}.
 */
@Configuration
public class UiadapterWebMvcConfig implements WebMvcConfigurer, WebMvcRegistrations {

    private static final Logger logger = LoggerFactory.getLogger(UiadapterWebMvcConfig.class);

    /** Spring ApplicationContext lookup helper used by uiadapter internals. */
    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    /** Replace Spring Boot's default multipart resolution with uiadapter's filter. */
    @Bean
    public MultipartFilter multipartFilter() {
        MultipartFilter multipartFilter = new MultipartFilter();
        multipartFilter.setMultipartResolverBeanName("multipartResolver");
        return multipartFilter;
    }

    /**
     * Register xeni's grid export/import servlet at {@code /XExportImport.do}.
     * Temp dir resolves to {@code java.io.tmpdir}
     * (Windows: {@code %USER%\AppData\Local\Temp}, Linux: {@code /tmp}).
     */
    @Bean
    public ServletRegistrationBean<GridExportImportServlet> gridExportImportServletBean() {
        ServletRegistrationBean<GridExportImportServlet> bean =
                new ServletRegistrationBean<>(new GridExportImportServlet(), "/XExportImport.do");
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

    /** Supply uiadapter's RequestMappingHandlerAdapter so @ParamDataSet/@ParamVariable resolve. */
    @Override
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return new NexacroRequestMappingHandlerAdapter();
    }

    /**
     * Register {@link NexacroMethodArgumentResolver}.
     *
     * <p>This MUST be wired even though {@link NexacroRequestMappingHandlerAdapter}
     * is supplied above: its {@code afterPropertiesSet()} reorders the resolver to
     * index 0 via {@code list.remove(indexOf(...))}, so if no resolver is in the
     * list yet the bootstrap fails with {@code IndexOutOfBoundsException: Index -1
     * out of bounds}.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new NexacroMethodArgumentResolver());
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
    }

    /**
     * Register {@link NexacroHandlerMethodReturnValueHandler} so controllers
     * returning {@link com.nexacro.uiadapter.jakarta.core.data.NexacroResult}
     * are serialised through xapi's {@link NexacroView} /
     * {@link NexacroFileView} / {@link NexacroStreamView}.
     */
    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
        NexacroHandlerMethodReturnValueHandler returnValueHandler =
                new NexacroHandlerMethodReturnValueHandler();

        NexacroView nexacroView = new NexacroView();
        nexacroView.setDefaultContentType(PlatformType.CONTENT_TYPE_XML); // CONTENT_TYPE_XML or CONTENT_TYPE_SSV
        nexacroView.setDefaultCharset("UTF-8");

        returnValueHandler.setView(nexacroView);
        returnValueHandler.setFileView(new NexacroFileView());
        returnValueHandler.setStreamView(new NexacroStreamView());

        handlers.add(returnValueHandler);
        WebMvcConfigurer.super.addReturnValueHandlers(handlers);
    }

    /**
     * Map handler exceptions to a properly framed Nexacro envelope so the
     * client always receives a structured ErrorCode/ErrorMsg.
     */
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

    /**
     * xeni excel module init params (mirrors the {@code <web.xml>} block of the
     * canonical sample). Controls temp directories used by the grid export/import
     * servlet and the cleanup monitor.
     */
    @Bean
    public ServletContextInitializer xeniExcelInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                servletContext.setInitParameter("export-path",        "/excel"); // export temp dir base
                servletContext.setInitParameter("import-path",        "/excel"); // import temp dir base
                servletContext.setInitParameter("monitor-enabled",    "true");   // enable temp file cleanup
                servletContext.setInitParameter("monitor-cycle-time", "30");     // monitor period (minutes)
                servletContext.setInitParameter("file-storage-time",  "10");     // temp file retention (minutes)
            }
        };
    }

    /**
     * DBMS metadata so MyBatis interceptors
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
