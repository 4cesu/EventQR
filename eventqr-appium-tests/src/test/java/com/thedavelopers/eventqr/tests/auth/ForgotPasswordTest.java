package com.thedavelopers.eventqr.tests.auth;

import com.thedavelopers.eventqr.base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordTest extends BaseTest {

    @Test
    @DisplayName("FRG-1: Forgot password screen loads with email field and submit")
    void forgotPasswordScreenLoads() {
        // TODO: verify forgot password screen element resource-ids in Appium Inspector
        assertAll(
                () -> assertTrue(isDisplayed(id("edtForgotEmail")), "Email field should be visible"),
                () -> assertTrue(isDisplayed(id("btnSendResetLink")), "Send reset link button should be visible")
        );
    }

    @Test
    @DisplayName("FRG-2: Invalid email shows validation error")
    void invalidEmailShowsError() {
        // TODO: verify forgot email validation error resource-id in Appium Inspector
        type(id("edtForgotEmail"), "notanemail");
        tap(id("btnSendResetLink"));
        assertTrue(isDisplayed(id("tilForgotEmail")) || isTextDisplayed("Enter a valid email"),
                "Invalid email error should be visible");
    }

    @Test
    @DisplayName("FRG-3: Valid email shows success confirmation")
    void validEmailShowsSuccess() {
        // TODO: verify success message resource-id in Appium Inspector
        type(id("edtForgotEmail"), "attendee@test.com");
        tap(id("btnSendResetLink"));
        assertTrue(isTextDisplayed("reset link")
                        || isTextDisplayed("sent")
                        || isTextDisplayed("check your email"),
                "Success confirmation should be visible");
    }
}
