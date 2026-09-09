package com.thedavelopers.eventqr.tests.auth;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest extends BaseTest {

    private LoginPage loginPage;

    private void initPages() {
        loginPage = new LoginPage();
    }

    protected void performLogin(String email, String pass) {
        initPages();
        loginPage.enterEmail(email);
        loginPage.enterPassword(pass);
        loginPage.tapSignIn();
    }

    @Test
    @DisplayName("LOGIN-1: Login screen shows logo, email, password, sign-in and forgot-password link")
    void loginScreenLoads() {
        initPages();
        assertAll(
                () -> assertTrue(loginPage.isLogoVisible(), "Logo should be visible"),
                () -> assertTrue(loginPage.isEmailFieldVisible(), "Email field should be visible"),
                () -> assertTrue(loginPage.isPasswordFieldVisible(), "Password field should be visible"),
                () -> assertTrue(loginPage.isSignInButtonVisible(), "Sign In button should be visible"),
                () -> assertTrue(loginPage.isForgotPasswordLinkVisible(), "Forgot Password link should be visible")
        );
    }

    @Test
    @DisplayName("LOGIN-2: Password show/hide toggle reveals masked password as plain text")
    void passwordToggleRevealsPlainText() {
        initPages();
        loginPage.enterPassword("secret123");
        assertFalse(loginPage.isPasswordVisibleAsPlainText(), "Password should be masked initially");
        loginPage.tapPasswordToggle();
        assertTrue(loginPage.isPasswordVisibleAsPlainText(), "Password should be visible as plain text after toggle");
        loginPage.tapPasswordToggle();
        assertFalse(loginPage.isPasswordVisibleAsPlainText(), "Password should be masked again after second toggle");
    }

    @Test
    @DisplayName("LOGIN-3: Empty email shows email field error")
    void emailEmptyShowsEmailError() {
        initPages();
        loginPage.enterPassword(TestConfig.ATTENDEE_PASS);
        loginPage.tapSignIn();
        assertTrue(loginPage.isEmailFieldErrorVisible(), "Email field error should be visible");
    }

    @Test
    @DisplayName("LOGIN-4: Empty password shows password field error")
    void passwordEmptyShowsPasswordError() {
        initPages();
        loginPage.enterEmail(TestConfig.ATTENDEE_EMAIL);
        loginPage.tapSignIn();
        assertTrue(loginPage.isPasswordFieldErrorVisible(), "Password field error should be visible");
    }

    @Test
    @DisplayName("LOGIN-5: Wrong credentials show error toast and stay on LoginActivity")
    void wrongCredentialsShowsError() {
        performLogin("wrong@test.com", "WrongPass123");
        assertTrue(loginPage.isFormErrorVisible(), "Error toast should be visible");
        assertTrue(loginPage.isCurrentActivity("LoginActivity"), "User should stay on LoginActivity");
    }

    @Test
    @DisplayName("LOGIN-6: Sign-in shows loading state and disables the button during submit")
    void signInShowsLoadingAndDisablesButton() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        assertTrue(isTextDisplayed("Signing in...") || isDisplayed(id("progressLogin")),
                "Loading indicator should appear");
        assertFalse(loginPage.isSignInButtonEnabled(), "Sign In button should be disabled while submitting");
    }

    @Test
    @DisplayName("LOGIN-7: Successful ATTENDEE login routes to attendee DashboardActivity")
    void attendeeLoginLandsOnDashboard() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        assertTrue(waitForActivity("features.dashboard.DashboardActivity"), "Attendee dashboard should open");
    }

    @Test
    @DisplayName("LOGIN-8: Successful STAFF login routes to StaffDashboardActivity")
    void staffLoginLandsOnDashboard() {
        performLogin(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        assertTrue(waitForActivity("StaffDashboardActivity"), "Staff dashboard should open");
    }

    @Test
    @DisplayName("LOGIN-9: Successful ORGANIZER login routes to OrganizerDashboardActivity")
    void organizerLoginLandsOnDashboard() {
        performLogin(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        assertTrue(waitForActivity("OrganizerDashboardActivity"), "Organizer dashboard should open");
    }

    @Test
    @DisplayName("LOGIN-10: Successful ADMIN/SUPER_ADMIN login routes to AdminDashboardActivity")
    void adminLoginLandsOnDashboard() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        assertTrue(waitForActivity("AdminDashboardActivity"), "Admin dashboard should open");
    }

    @Test
    @DisplayName("LOGIN-11: Forgot Password link navigates to ForgotPasswordActivity")
    void forgotPasswordNavigates() {
        initPages();
        loginPage.tapForgotPassword();
        assertTrue(isDisplayed(id("editEmail")), "Forgot password email field should be visible");
    }

    @Test
    @DisplayName("LOGIN-12: Register link navigates to RegistrationActivity")
    void registerLinkNavigates() {
        initPages();
        loginPage.tapRegister();
        assertAll(
                () -> assertTrue(isDisplayed(id("edtFirstName")), "Registration first name field should be visible"),
                () -> assertTrue(isDisplayed(id("edtLastName")), "Registration last name field should be visible")
        );
    }
}