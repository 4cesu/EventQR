package com.thedavelopers.eventqr.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import brevo.ApiClient;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalEmailsApi;

@org.springframework.context.annotation.Configuration
public class BrevoConfig {

    @Bean
    public ApiClient brevoApiClient(@Value("${brevo.api.key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY must be configured");
        }
        ApiClient apiClient = new ApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) apiClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);
        return apiClient;
    }

    @Bean
    public TransactionalEmailsApi transactionalEmailsApi(ApiClient brevoApiClient) {
        return new TransactionalEmailsApi(brevoApiClient);
    }
}