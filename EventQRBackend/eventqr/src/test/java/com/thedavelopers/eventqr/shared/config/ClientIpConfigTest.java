package com.thedavelopers.eventqr.shared.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.thedavelopers.eventqr.shared.security.ClientIp;

import jakarta.servlet.http.HttpServletRequest;

class ClientIpConfigTest {

    @AfterEach
    void reset() {
        ClientIp.configureTrustedHeader(null);
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("CF-Connecting-IP")).thenReturn("203.0.113.7");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.42");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        return request;
    }

    @Test
    void theConfiguredHeaderIsAppliedAtStartup() {
        new ClientIpConfig("CF-Connecting-IP");

        assertThat(ClientIp.from(request())).isEqualTo("203.0.113.7");
    }

    @Test
    void anEmptySettingTurnsTheFeatureOff() {
        new ClientIpConfig("CF-Connecting-IP");
        new ClientIpConfig("");

        assertThat(ClientIp.from(request())).isEqualTo("198.51.100.42");
    }
}
