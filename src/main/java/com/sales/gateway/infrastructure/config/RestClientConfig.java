package com.sales.gateway.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient contractRestClient(
            RestClient.Builder builder,
            @Value("${app.services.contract-api.url}") String contractApiUrl) {
        return builder.baseUrl(contractApiUrl).build();
    }
}
