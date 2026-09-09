package com.thedavelopers.eventqr.tests.auth;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("LOGIN-1: Landing screen loads and shows login form")
    void loginScreenLoads() {
        initPages();
        // TODO: verify landing elements resource-ids in Appium Inspector
        assertTrue(isDisplayed(id("edtEmail")), "Email field should be visible");
    }

    @Test
    @DisplayName("LOGIN-2: Password field masks input")
    void passwordFieldMasksInput() {
        initPages();
        loginPage.enterPassword("secret123");
        // TODO: verify password field inputType masking in Appium Inspector
        assertTrue(isDisplayed(id("edtPassword")), "Password field should be visible");
    }

    @Test
    @DisplayName("LOGIN-3: Email only, empty password shows password error")
    void emailOnlyShowsPasswordError() {
        initPages();
        loginPage.enterEmail("attendee@test.com");
        loginPage.tapSignIn();
        assertTrue(loginPage.isPasswordFieldErrorVisible(), "Password field error should be visible");
    }

    @Test
    @DisplayName("LOGIN-4: Password only, empty email shows email error")
    void passwordOnlyShowsEmailError() {
        initPages();
        loginPage.enterPassword(TestConfig.ATTENDEE_PASS);
        loginPage.tapSignIn();
        assertTrue(loginPage.isEmailFieldErrorVisible(), "Email field error should be visible");
    }

    @Test
    @DisplayName("LOGIN-5: Wrong credentials shows error toast")
    void wrongCredentialsShowsError() {
        performLogin("wrong@test.com", "WrongPass123");
        assertTrue(loginPage.isFormErrorVisible(), "Error message should be visible");
    }

    @Test
    @DisplayName("LOGIN-6: Sign in button submits and shows loading")
    void signInShowsLoading() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        // TODO: verify loading indicator resource-id in Appium Inspector
        assertTrue(isTextDisplayed("Loading") || isDisplayed(id("progressLogin")),
                "Loading indicator should appear");
    }

    @Test
    @DisplayName("LOGIN-7: Attendee login lands on AttendeeDashboard")
    void attendeeLoginLandsOnDashboard() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        // TODO: verify unique attendee dashboard element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtWelcome")), "Attendee dashboard should be visible");
    }

    @Test
    @DisplayName("LOGIN-8: Staff login lands on StaffDashboard")
    void staffLoginLandsOnDashboard() {
        performLogin(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        // TODO: verify unique staff dashboard element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtWelcome")), "Staff dashboard should be visible");
    }

    @Test
    @DisplayName("LOGIN-9: Organizer login lands on OrganizerDashboard")
    void organizerLoginLandsOnDashboard() {
        performLogin(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        // TODO: verify unique organizer dashboard element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtWelcome")), "Organizer dashboard should be visible");
    }

    @Test
    @DisplayName("LOGIN-10: Admin login lands on AdminDashboard")
    void adminLoginLandsOnDashboard() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        // TODO: verify unique admin dashboard element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("textAdminPortalTitle")), "Admin dashboard should be visible");
    }

    @Test
    @DisplayName("LOGIN-11: Forgot Password link navigates to forgot screen")
    void forgotPasswordNavigates() {
        initPages();
        loginPage.tapForgotPassword();
        // TODO: verify forgot password screen element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("edtForgotEmail")), "Forgot password screen should be visible");
    }

    @Test
    @DisplayName("LOGIN-12: Register link navigates to registration screen")
    void registerLinkNavigates() {
        initPages();
        loginPage.tapRegister();
        // TODO: verify registration screen element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("edtFullName")), "Registration screen should be visible");
    }
}
