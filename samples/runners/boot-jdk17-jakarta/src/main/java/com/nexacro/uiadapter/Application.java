package com.nexacro.uiadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the boot-jdk17-jakarta runner.
 *
 * <p>Component scan defaults to {@code com.nexacro.uiadapter} (this package + sub-packages),
 * which covers the canonical layout used in the upstream nexacron GitLab sample:
 * {@code config}, {@code controller}, {@code service}, {@code service.impl},
 * {@code mapper}, {@code domain}.
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
