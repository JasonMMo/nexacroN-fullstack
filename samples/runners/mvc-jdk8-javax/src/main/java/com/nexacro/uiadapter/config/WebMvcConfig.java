package com.nexacro.uiadapter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import com.nexacro.uiadapter.spring.core.resolve.NexacroRequestMappingHandlerAdapter;

/**
 * Web MVC context: controllers and MVC infrastructure.
 * Static resources at /packageN/** are served from classpath:/static/packageN/.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.nexacro.uiadapter.controller")
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/packageN/**")
                .addResourceLocations("classpath:/static/packageN/");
        // Serve images and videos under /static/**
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @org.springframework.context.annotation.Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        return new NexacroRequestMappingHandlerAdapter();
    }
}
