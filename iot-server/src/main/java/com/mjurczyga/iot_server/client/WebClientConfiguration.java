package com.mjurczyga.iot_server.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Value("${HOME_ASSISTANT_BASE_URL}")
    private String baseUrl;
    
    @Value("${HOME_ASSISTANT_TOKEN}")
    private String token;

    @Bean
    public WebClient webClient(){
       return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}