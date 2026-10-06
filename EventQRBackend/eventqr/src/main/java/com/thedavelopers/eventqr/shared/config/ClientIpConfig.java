package com.thedavelopers.eventqr.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.thedavelopers.eventqr.shared.security.ClientIp;

/** Applies {@code app.client-ip.trusted-header} to {@link ClientIp} at startup. */
@Component
public class ClientIpConfig {

    public ClientIpConfig(@Value("${app.client-ip.trusted-header:}") String trustedHeader) {
        ClientIp.configureTrustedHeader(trustedHeader);
    }
}
