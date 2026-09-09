package com.thedavelopers.eventqr.features.auth.controller;

import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.auth.model.dto.LoginRequest;
import com.thedavelopers.eventqr.features.auth.model.dto.LoginResponse;
import com.thedavelopers.eventqr.features.auth.model.dto.ChangePasswordRequest;
import com.thedavelopers.eventqr.features.auth.model.dto.ForgotPasswordRequest;
import com.thedavelopers.eventqr.features.auth.model.dto.RegisterRequest;
import com.thedavelopers.eventqr.features.auth.model.dto.ResetPasswordRequest;
import com.thedavelopers.eventqr.features.auth.service.AuthService;
import com.thedavelopers.eventqr.features.auth.service.ChangePasswordService;
import com.thedavelopers.eventqr.features.auth.service.PasswordResetService;
import com.thedavelopers.eventqr.features.users.model.dto.PasswordChangeRequest;
import com.thedavelopers.eventqr.features.users.model.dto.UserRequest;
import com.thedavelopers.eventqr.features.users.model.dto.UserResponse;
import com.thedavelopers.eventqr.features.users.service.UserService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.ForgotPasswordRateLimiter;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;
    private final ChangePasswordService changePasswordService;
    private final ForgotPasswordRateLimiter forgotPasswordRateLimiter;

    public AuthController(AuthService authService, UserService userService, JwtService jwtService,
                          PasswordResetService passwordResetService, ChangePasswordService changePasswordService,
                          ForgotPasswordRateLimiter forgotPasswordRateLimiter) {
        this.authService = authService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
        this.changePasswordService = changePasswordService;
        this.forgotPasswordRateLimiter = forgotPasswordRateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserRequest userRequest = new UserRequest(request.email(), request.fullName(), request.phoneNumber(), request.password(), AccountRole.ATTENDEE);
        return ResponseEntity.ok(ApiResponse.success("Registration completed", userService.create(userRequest)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login processed", authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(HttpServletRequest request) {
        UUID userId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        return ResponseEntity.ok(ApiResponse.success(userService.findOne(userId)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request) {
        UUID userId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        return ResponseEntity.ok(ApiResponse.success("Session refreshed", authService.refreshToken(userId)));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(HttpServletRequest request,
                                                                    @Valid @RequestBody PasswordChangeRequest body) {
        UUID userId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        return ResponseEntity.ok(ApiResponse.success("Password updated", userService.changePassword(userId, body.currentPassword(), body.newPassword())));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePasswordWithConfirm(HttpServletRequest request,
                                                                       @Valid @RequestBody ChangePasswordRequest body) {
        UUID userId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        changePasswordService.changePassword(userId, body.currentPassword(), body.newPassword(), body.confirmPassword());
        return ResponseEntity.ok(ApiResponse.success("Password has been changed", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        jwtService.revoke(request.getHeader("Authorization"));
        return ResponseEntity.ok(ApiResponse.success("Logout processed", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(HttpServletRequest request,
                                                            @Valid @RequestBody ForgotPasswordRequest forgotPassword) {
        if (!forgotPasswordRateLimiter.allow(request, forgotPassword.email())) {
            throw new com.thedavelopers.eventqr.shared.exceptions.TooManyRequestsException(
                    "Too many password reset requests. Please try again later.");
        }
        passwordResetService.requestReset(forgotPassword.email());
        return ResponseEntity.ok(ApiResponse.success("If an account with that email exists, a reset link has been sent", null));
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateResetToken(@RequestParam String token) {
        boolean valid = passwordResetService.validateToken(token);
        if (!valid) {
            return ResponseEntity.badRequest().body(ApiResponse.success("Token is invalid or expired", Map.of("valid", false)));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("valid", true)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword(), request.confirmPassword());
        return ResponseEntity.ok(ApiResponse.success("Password has been reset", null));
    }
}
