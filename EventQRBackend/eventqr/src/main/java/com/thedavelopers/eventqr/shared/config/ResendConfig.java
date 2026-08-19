package com.thedavelopers.eventqr.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

@Configuration
public class ResendConfig {

    @Bean
    public Resend resend(@Value("${resend.api.key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("MAIL_RESEND_API_KEY must be configured");
        }
        return new Resend(apiKey);
    }
}