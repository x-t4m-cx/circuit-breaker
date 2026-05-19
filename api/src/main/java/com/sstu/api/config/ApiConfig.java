package com.sstu.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiConfig {

    @Value("${data.url}")
    private String url;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
