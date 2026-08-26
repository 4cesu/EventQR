package com.thedavelopers.eventqr.features.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thedavelopers.eventqr.features.auth.model.entity.PasswordResetToken;
import com.thedavelopers.eventqr.features.auth.repository.PasswordResetTokenRepository;
import com.thedavelopers.eventqr.features.qremail.service.EmailGatewayService;
import com.thedavelopers.eventqr.features.users.model.entity.UserProfile;
import com.thedavelopers.eventqr.features.users.repository.UserProfileRepository;
import com.thedavelopers.eventqr.shared.exceptions.BadRequestException;
import com.thedavelopers.eventqr.shared.utils.PasswordValidator;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final Duration RESET_TTL = Duration.ofMinutes(30);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailGatewayService emailGatewayService;
    private final String frontendBaseUrl;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository,
                                UserProfileRepository userProfileRepository,
                                PasswordEncoder passwordEncoder,
                                EmailGatewayService emailGatewayService,
                                @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailGatewayService = emailGatewayService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void requestReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        Optional<UserProfile> userOpt = userProfileRepository.findByEmailIgnoreCase(normalizedEmail);
        if (userOpt.isEmpty()) {
            log.debug("Password reset requested for unknown email, returning silently");
            return;
        }
        UserProfile user = userOpt.get();
        passwordResetTokenRepository.invalidateAllUnusedByUserId(user.getId());
        String token = generateToken();
        Instant expiresAt = Instant.now().plus(RESET_TTL);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setToken(token);
        resetToken.setExpiresAt(expiresAt);
        resetToken.setUsed(false);
        resetToken.setCreatedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
        String subject = "EventQR — Reset your password";
        String html = """
                <!doctype html>
                <html><body>
                <p>Hello %s,</p>
                <p>We received a request to reset your password. Click the link below to set a new password. This link expires in 30 minutes.</p>
                <p><a href="%s" style="display:inline-block;padding:10px 20px;background:#6C63FF;color:#FFFFFF;text-decoration:none;border-radius:6px;">Reset Password</a></p>
                <p>If you did not request this, you can safely ignore this email.</p>
                <p>— The EventQR Team</p>
                </body></html>
                """.formatted(user.getFullName(), resetLink);
        try {
            emailGatewayService.sendSimple(normalizedEmail, subject, html);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", normalizedEmail, e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return passwordResetTokenRepository
                .findByTokenAndUsedFalseAndExpiresAtAfter(token.trim(), Instant.now())
                .isPresent();
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Reset token is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("New password is required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }
        if (!PasswordValidator.isValid(newPassword)) {
            throw new BadRequestException(PasswordValidator.FAILURE_MESSAGE);
        }
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalseAndExpiresAtAfter(token.trim(), Instant.now())
                .orElseThrow(() -> new BadRequestException("Reset token is invalid or expired"));
        UserProfile user = userProfileRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BadRequestException("User account not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userProfileRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return email.trim().toLowerCase();
    }
}
