package com.nexacro.fullstack.runner.boot17;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.nexacro.fullstack.runner.boot17",
    "com.nexacro.fullstack.business"
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
