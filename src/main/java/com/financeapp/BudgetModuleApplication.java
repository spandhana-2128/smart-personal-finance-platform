package com.financeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.financeapp.model.SystemConfig;

@SpringBootApplication
public class BudgetModuleApplication {

    public static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        SystemConfig config = SystemConfig.getInstance();
        System.out.println("System starting with DB: " + config.getDbConnection());
        // Start Spring Boot first, then launch JavaFX
        context = new SpringApplicationBuilder(BudgetModuleApplication.class)
                .headless(false)
                .run(args);
        // Launch JavaFX
        javafx.application.Application.launch(JavaFXApp.class, args);
    }
}