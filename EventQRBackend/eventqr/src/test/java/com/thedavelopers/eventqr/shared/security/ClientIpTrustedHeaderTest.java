package com.thedavelopers.eventqr.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

/** On Render the edge writes CF-Connecting-IP; the resolver must prefer it but never be fooled. */
@ExtendWith(MockitoExtension.class)
class ClientIpTrustedHeaderTest {

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void enable() {
        ClientIp.configureTrustedHeader("CF-Connecting-IP");
    }

    @AfterEach
    void reset() {
        ClientIp.configureTrustedHeader(null); // static state: leave it as other tests expect
    }

    private void headers(String trusted, String xff, String remote) {
        lenient().when(request.getHeader("CF-Connecting-IP")).thenReturn(trusted);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(xff);
        lenient().when(request.getRemoteAddr()).thenReturn(remote);
    }

    @Test
    void theTrustedHeaderWinsOverForwardedFor() {
        headers("203.0.113.7", "198.51.100.1, 10.1.1.1", "10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("203.0.113.7");
    }

    @Test
    void aCallerSpoofingForwardedForCannotChangeTheResult() {
        // The caller controls the left of X-Forwarded-For; the edge-written header is unaffected.
        headers("203.0.113.7", "6.6.6.6, 203.0.113.7", "10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("203.0.113.7");
    }

    @Test
    void ipv6AndWhitespaceAreAccepted() {
        headers("  2001:db8::1  ", null, "10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("2001:db8::1");
    }

    @Test
    void aGarbageHeaderIsIgnoredAndTheNextSourceIsUsed() {
        headers("not-an-ip", "198.51.100.42", "10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("198.51.100.42");
    }

    @Test
    void aListInTheTrustedHeaderIsNotAccepted() {
        // A single address is expected; a comma list would let a caller choose its own bucket.
        headers("1.1.1.1, 2.2.2.2", null, "10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("10.0.0.1");
    }

    @Test
    void anAbsentHeaderFallsBackToForwardedFor() {
        headers(null, "198.51.100.42", "10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("198.51.100.42");
    }

    @Test
    void withTheFeatureOffTheHeaderIsIgnoredEvenIfPresent() {
        ClientIp.configureTrustedHeader("");
        headers("203.0.113.7", "198.51.100.42", "10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("198.51.100.42");
    }

    @Test
    void aDifferentConfiguredHeaderNameIsHonoured() {
        ClientIp.configureTrustedHeader("X-Real-Client-IP");
        lenient().when(request.getHeader("X-Real-Client-IP")).thenReturn("192.0.2.9");
        lenient().when(request.getHeader("CF-Connecting-IP")).thenReturn("203.0.113.7");
        lenient().when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        assertThat(ClientIp.from(request)).isEqualTo("192.0.2.9");
    }
}
