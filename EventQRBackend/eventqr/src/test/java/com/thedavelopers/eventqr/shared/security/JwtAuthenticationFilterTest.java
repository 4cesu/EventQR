package com.thedavelopers.eventqr.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.thedavelopers.eventqr.shared.constants.AccountRole;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "01234567890123456789012345678901"; // 32 bytes (HS256 min)

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static String bearerToken(JwtService service) {
        return "Bearer " + service.createToken(UUID.randomUUID(), "user@example.com", AccountRole.ATTENDEE);
    }

    private static FilterChain noOpChain() {
        return (request, response) -> {
            // no-op
        };
    }

    @Test
    void revokedTokenDoesNotSetAuthentication() throws Exception {
        JwtService jwtService = new JwtService(SECRET, 86400000L);
        String bearer = bearerToken(jwtService);
        jwtService.revoke(bearer);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", bearer);
        filter.doFilter(request, new MockHttpServletResponse(), noOpChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validNotRevokedTokenSetsAuthentication() throws Exception {
        JwtService jwtService = new JwtService(SECRET, 86400000L);
        String bearer = bearerToken(jwtService);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", bearer);
        filter.doFilter(request, new MockHttpServletResponse(), noOpChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void missingHeaderLeavesContextClear() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new JwtService(SECRET, 86400000L));
        MockHttpServletRequest request = new MockHttpServletRequest();
        filter.doFilter(request, new MockHttpServletResponse(), noOpChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}