package com.rkudrin.teststudybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class TestStudyBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestStudyBotApplication.class, args);
    }

}
