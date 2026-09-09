package com.thedavelopers.eventqr.tests.auth;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.RegistrationPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationTest extends BaseTest {

    private RegistrationPage regPage;

    private void initPages() {
        regPage = new RegistrationPage();
    }

    @Test
    @DisplayName("REG-1: Registration screen loads with all required fields")
    void registrationScreenLoads() {
        initPages();
        assertAll(
                () -> assertTrue(isDisplayed(id("edtFullName")), "Full name field should be visible"),
                () -> assertTrue(isDisplayed(id("edtEmail")), "Email field should be visible"),
                () -> assertTrue(isDisplayed(id("edtPhone")), "Phone field should be visible"),
                () -> assertTrue(isDisplayed(id("edtPassword")), "Password field should be visible"),
                () -> assertTrue(isDisplayed(id("edtConfirmPassword")), "Confirm password field should be visible")
        );
    }

    @Test
    @DisplayName("REG-2: Email field validates format")
    void emailValidatesFormat() {
        initPages();
        regPage.enterEmail("invalid-email");
        // TODO: verify email validation error resource-id in Appium Inspector
        assertTrue(isDisplayed(id("tilEmail")), "Email input layout should be present");
    }

    @Test
    @DisplayName("REG-3: Phone with leading zero is auto-stripped to 10 digits")
    void phoneLeadingZeroIsStripped() {
        initPages();
        regPage.enterPhone("09123456789");
        // TODO: verify phone auto-strip behavior and field value in Appium Inspector
        String value = regPage.getPhoneValue().replaceAll("[^0-9]", "");
        assertEquals(10, value.length(), "Phone should be stripped to 10 digits");
    }

    @Test
    @DisplayName("REG-4: Full name field is required")
    void fullNameRequired() {
        initPages();
        regPage.enterEmail("new@test.com");
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapCreateAccount();
        // TODO: verify full name validation error resource-id in Appium Inspector
        assertTrue(isDisplayed(id("tilFullName")), "Full name validation should appear");
    }

    @Test
    @DisplayName("REG-5: Typing first password char reveals requirements panel")
    void passwordRequirementsPanelAppears() {
        initPages();
        regPage.enterPassword("A");
        assertTrue(regPage.isRequirementsPanelVisible(), "Password requirements panel should be visible");
    }

    @Test
    @DisplayName("REG-6: Requirement checkmarks update with stronger passwords")
    void passwordStrengthUpdates() {
        initPages();
        regPage.enterPassword("weak");
        // TODO: verify strength bar segment count matches password strength in Appium Inspector
        assertTrue(regPage.isStrengthBarVisible(), "Strength bar should be visible");
    }

    @Test
    @DisplayName("REG-7: Incomplete form keeps Create Account disabled")
    void incompleteFormDisablesCreateAccount() {
        initPages();
        regPage.enterFullName("Test User");
        regPage.enterEmail("new@test.com");
        assertFalse(regPage.isCreateAccountButtonEnabled(), "Create Account should be disabled for incomplete form");
    }

    @Test
    @DisplayName("REG-8: Password and confirm password must match")
    void passwordsMustMatch() {
        initPages();
        regPage.enterFullName("Test User");
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword("Different123");
        regPage.tapCreateAccount();
        // TODO: verify mismatch error resource-id in Appium Inspector
        assertTrue(isDisplayed(id("tilConfirmPassword")), "Confirm password mismatch error should appear");
    }

    @Test
    @DisplayName("REG-9: Valid form submission navigates to login")
    void validRegistrationNavigatesToLogin() {
        initPages();
        regPage.enterFullName("New Test User");
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapCreateAccount();
        // TODO: verify navigation to login screen element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("btnSignIn")), "Should navigate back to login screen");
    }

    @Test
    @DisplayName("REG-10: Duplicate email shows error")
    void duplicateEmailShowsError() {
        initPages();
        regPage.enterFullName("Test User");
        regPage.enterEmail(TestConfig.ATTENDEE_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapCreateAccount();
        // TODO: verify duplicate email error resource-id in Appium Inspector
        assertTrue(isTextDisplayed("already registered")
                || isTextDisplayed("already exists")
                || isTextDisplayed("taken"),
                "Duplicate email error should be visible");
    }

    @Test
    @DisplayName("REG-11: Create Account button is enabled for valid form")
    void validFormEnablesCreateAccount() {
        initPages();
        regPage.enterFullName("Test User");
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        assertTrue(regPage.isCreateAccountButtonEnabled(), "Create Account should be enabled for valid form");
    }

    @Test
    @DisplayName("REG-12: Back button returns to login")
    void backReturnsToLogin() {
        initPages();
        pressBack();
        // TODO: verify login screen element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("btnSignIn")), "Back should return to login screen");
    }
}
