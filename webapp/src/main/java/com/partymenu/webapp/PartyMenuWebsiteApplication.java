package com.partymenu.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class PartyMenuWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartyMenuWebsiteApplication.class, args);
    }
}
