package com.thedavelopers.eventqr.shared.utils;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private PasswordValidator() {
    }

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static final String FAILURE_MESSAGE =
            "Password must be at least 8 characters and include an uppercase letter, a number, and a special character";
}
