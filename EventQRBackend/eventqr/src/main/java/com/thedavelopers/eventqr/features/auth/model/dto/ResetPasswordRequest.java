package com.thedavelopers.eventqr.features.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(@NotBlank String token,
                                   @NotBlank @Size(min = 8) @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                                           message = "Password must be at least 8 characters and include an uppercase letter, a number, and a special character") String newPassword,
                                   @NotBlank @Size(min = 8) String confirmPassword) {
}
