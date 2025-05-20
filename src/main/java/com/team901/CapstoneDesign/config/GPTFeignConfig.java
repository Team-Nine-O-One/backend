package com.team901.CapstoneDesign.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GPTFeignConfig {
    @Value(value = "${gpt.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor gptRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + apiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

}
