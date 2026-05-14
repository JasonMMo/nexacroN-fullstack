package com.nexacro.uiadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot entry point for the egov4-boot-jdk8-javax runner.
 *
 * <p>Component scan spans {@code com.nexacro.uiadapter} (canonical layout:
 * {@code config}, {@code controller}, {@code service}, {@code service.impl},
 * {@code mapper}, {@code domain}) and {@code egovframework} so the minimal
 * eGov 4 scaffold under {@code egovframework.com.config} is picked up.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.nexacro.uiadapter", "egovframework"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
