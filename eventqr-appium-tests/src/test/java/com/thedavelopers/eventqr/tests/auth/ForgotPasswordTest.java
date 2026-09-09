package com.thedavelopers.eventqr.tests.auth;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordTest extends BaseTest {

    private void openForgotPasswordScreen() {
        tap(id("txtForgotPassword"));
    }

    @Test
    @DisplayName("FRG-1: Forgot password screen shows title, email field, submit and back buttons")
    void forgotPasswordScreenLoads() {
        openForgotPasswordScreen();
        assertAll(
                () -> assertTrue(isTextDisplayed("Forgot Password"), "Title should be visible"),
                () -> assertTrue(isDisplayed(id("editEmail")), "Email field should be visible"),
                () -> assertTrue(isDisplayed(id("btnSendResetLink")), "Send reset link button should be visible"),
                () -> assertTrue(isDisplayed(id("btnBackToSignIn")), "Back button should be visible")
        );
    }

    @Test
    @DisplayName("FRG-2: Valid email submission shows reset-link success confirmation")
    void validEmailShowsSuccess() {
        openForgotPasswordScreen();
        type(id("editEmail"), TestConfig.ATTENDEE_EMAIL);
        tap(id("btnSendResetLink"));
        assertTrue(isDisplayed(id("layoutConfirmation")) || isTextDisplayed("Check your email"),
                "Reset-link sent confirmation should be visible");
    }

    @Test
    @DisplayName("FRG-2b: Invalid email shows validation error")
    void invalidEmailShowsError() {
        openForgotPasswordScreen();
        type(id("editEmail"), "notanemail");
        tap(id("btnSendResetLink"));
        assertTrue(isTextDisplayed("Enter a valid email address"),
                "Invalid email error should be visible");
    }

    @Test
    @DisplayName("FRG-3: Back button returns to LoginActivity")
    void backReturnsToLogin() {
        openForgotPasswordScreen();
        tap(id("btnBackToSignIn"));
        assertTrue(waitForActivity("LoginActivity"), "Back should return to login screen");
    }
}