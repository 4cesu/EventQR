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

    /** Fills first/last name, valid email, and matching valid password+confirm. */
    private void fillNameEmailPassword() {
        regPage.enterFirstName("New");
        regPage.enterLastName("Test User");
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
    }

    @Test
    @DisplayName("REG-1: Registration screen shows first/last name, email, phone, passwords, create account and sign-in link")
    void registrationScreenLoads() {
        initPages();
        assertAll(
                () -> assertTrue(isDisplayed(id("edtFirstName")), "First name field should be visible"),
                () -> assertTrue(isDisplayed(id("edtLastName")), "Last name field should be visible"),
                () -> assertTrue(isDisplayed(id("edtEmail")), "Email field should be visible"),
                () -> assertTrue(isDisplayed(id("edtPhoneNumber")), "Phone field should be visible"),
                () -> assertTrue(isDisplayed(id("edtPassword")), "Password field should be visible"),
                () -> assertTrue(isDisplayed(id("edtConfirmPassword")), "Confirm password field should be visible"),
                () -> assertTrue(regPage.isTermsCheckboxDisplayed(), "Terms checkbox should be visible"),
                () -> assertTrue(isDisplayed(id("btnRegister")), "Create Account button should be visible"),
                () -> assertTrue(isDisplayed(id("btnSignIn")), "Sign In link should be visible")
        );
    }

    @Test
    @DisplayName("REG-2: Password show/hide toggles reveal both password and confirm fields as plain text")
    void passwordShowHideToggles() {
        initPages();
        regPage.enterPassword("Secret123!");
        regPage.enterConfirmPassword("Secret123!");
        assertFalse(regPage.isPasswordVisibleAsPlainText(), "Password should be masked initially");
        regPage.tapPasswordToggle();
        assertTrue(regPage.isPasswordVisibleAsPlainText(), "Password should be visible after toggle");
        regPage.tapPasswordToggle();
        assertFalse(regPage.isPasswordVisibleAsPlainText(), "Password should be masked after second toggle");
        assertFalse(regPage.isConfirmPasswordVisibleAsPlainText(), "Confirm password should be masked initially");
        regPage.tapConfirmPasswordToggle();
        assertTrue(regPage.isConfirmPasswordVisibleAsPlainText(), "Confirm password should be visible after toggle");
        regPage.tapConfirmPasswordToggle();
        assertFalse(regPage.isConfirmPasswordVisibleAsPlainText(), "Confirm password should be masked after second toggle");
    }

    @Test
    @DisplayName("REG-3: Phone auto-normalizes leading 0 with live counter; invalid length shows error")
    void phoneValidation() {
        initPages();
        regPage.enterPhone("0912345678");
        assertEquals("912345678", regPage.getPhoneValue(), "Leading zero should be stripped");
        assertEquals("9/10", regPage.getPhoneCounterText(), "Live n/10 counter should reflect normalized digits");

        regPage.enterPhone("123");
        fillNameEmailPassword();
        regPage.tapTermsCheckbox();
        regPage.tapCreateAccount();
        assertTrue(regPage.isPhoneErrorVisible(), "Enter valid 10-digit mobile number should appear");
    }

    @Test
    @DisplayName("REG-4: First and last name are required")
    void fullNameRequired() {
        initPages();
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapTermsCheckbox();
        regPage.tapCreateAccount();
        assertTrue(regPage.isFirstNameErrorVisible(), "First name required error should appear");
    }

    @Test
    @DisplayName("REG-5: Email field is required")
    void emailRequired() {
        initPages();
        regPage.enterFirstName("New");
        regPage.enterLastName("Test User");
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapTermsCheckbox();
        regPage.tapCreateAccount();
        assertTrue(regPage.isEmailErrorVisible(), "Email required error should appear");
    }

    @Test
    @DisplayName("REG-6: Password is required - Create Account stays disabled")
    void passwordRequired() {
        initPages();
        regPage.enterFirstName("New");
        regPage.enterLastName("Test User");
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterConfirmPassword("");
        assertFalse(regPage.isCreateAccountButtonEnabled(), "Create Account should be disabled with empty password");
    }

    @Test
    @DisplayName("REG-7: Confirm password is required - mismatch error when left empty")
    void confirmPasswordRequired() {
        initPages();
        regPage.enterFirstName("New");
        regPage.enterLastName("Test User");
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapTermsCheckbox();
        regPage.tapCreateAccount();
        assertTrue(regPage.isConfirmPasswordErrorVisible(), "Confirm password mismatch error should appear");
    }

    @Test
    @DisplayName("REG-8: Mismatched passwords show specific 'Passwords do not match' error")
    void passwordsMustMatch() {
        initPages();
        regPage.enterFirstName("New");
        regPage.enterLastName("Test User");
        regPage.enterEmail(TestConfig.NEW_USER_EMAIL);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword("Different123");
        regPage.tapTermsCheckbox();
        regPage.tapCreateAccount();
        assertTrue(regPage.isConfirmPasswordErrorVisible(), "Passwords do not match error should appear");
    }

    @Test
    @DisplayName("REG-9: Terms unchecked - Create Account stays disabled")
    void termsUncheckedBlocksSubmit() {
        initPages();
        fillNameEmailPassword();
        regPage.enterPhone("9123456789");
        assertFalse(regPage.isCreateAccountButtonEnabled(),
                "Create Account should stay disabled while terms unchecked");
    }

    @Test
    @DisplayName("REG-10: All valid fields + terms checked enable Create Account")
    void validFormWithTermsEnablesCreateAccount() {
        initPages();
        fillNameEmailPassword();
        regPage.enterPhone("9123456789");
        regPage.tapTermsCheckbox();
        assertTrue(regPage.isCreateAccountButtonEnabled(),
                "Create Account should be enabled with valid form and terms checked");
    }

    @Test
    @DisplayName("REG-11: Valid submission shows success toast and navigates to LoginActivity")
    void validRegistrationNavigatesToLogin() {
        initPages();
        // Fresh unique email per run so reruns never collide with earlier registrations.
        String freshEmail = "newuser_" + System.currentTimeMillis() + "@test.com";
        regPage.enterFirstName("New");
        regPage.enterLastName("Test User");
        regPage.enterEmail(freshEmail);
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapTermsCheckbox();
        regPage.tapCreateAccount();
        assertTrue(isTextDisplayed("Registration completed") || isTextDisplayed("Account created"),
                "Success toast should be visible");
        assertTrue(waitForActivity("LoginActivity"), "Should navigate back to LoginActivity");
    }

    @Test
    @DisplayName("REG-12: Invalid email format shows email field error")
    void emailFormatValidated() {
        initPages();
        regPage.enterFirstName("New");
        regPage.enterLastName("Test User");
        regPage.enterEmail("invalid-email");
        regPage.enterPhone("9123456789");
        regPage.enterPassword(TestConfig.ATTENDEE_PASS);
        regPage.enterConfirmPassword(TestConfig.ATTENDEE_PASS);
        regPage.tapTermsCheckbox();
        regPage.tapCreateAccount();
        assertTrue(regPage.isEmailErrorVisible(), "Email format error should be visible");
    }
}