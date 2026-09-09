package com.thedavelopers.eventqr.tests.admin;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AdminDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

public class AdminAccountManagementTest extends BaseTest {

    private AdminDashboardPage dash;

    @BeforeEach
    void loginAsSuperAdminAndOpenAccounts() {
        LoginPage login = new LoginPage();
        // buttonCreateAdminAccount is only rendered for SUPER_ADMIN accounts
        // (AdminAccountManagementActivity.isSuperAdmin()); the CAA tests depend on it.
        login.login(TestConfig.SUPERADMIN_EMAIL, TestConfig.SUPERADMIN_PASS);
        dash = new AdminDashboardPage();
        dash.openAccountsTab();
    }

    @Test
    @DisplayName("AAM-1: User account list loads")
    void accountListLoads() {
        assertTrue(isDisplayed(id("recyclerAdminAccounts")), "Account list should be visible");
    }

    @Test
    @DisplayName("AAM-2: Filter chips displayed for account roles")
    void filterChipsDisplayed() {
        assertTrue(isDisplayed(id("filterChipsContainer")), "Role filter chips should be visible");
    }

    @Test
    @DisplayName("AAM-3: Search accounts by name or email")
    void searchAccounts() {
        type(id("inputAccountSearch"), "Test");
        assertTrue(isDisplayed(id("recyclerAdminAccounts")), "Search results list should be visible");
    }

    @Test
    @DisplayName("AAM-4: Swipe down refreshes account list")
    void swipeDownRefreshes() {
        swipeDown();
        assertTrue(isDisplayed(id("recyclerAdminAccounts")), "Account list should remain after refresh");
    }

    @Test
    @DisplayName("CAA-1: Create admin account form loads")
    void createAdminFormLoads() {
        tap(id("buttonCreateAdminAccount"));
        assertAll(
                () -> assertTrue(isDisplayed(id("inputAdminFirstName")), "First name field should be visible"),
                () -> assertTrue(isDisplayed(id("inputAdminEmail")), "Email field should be visible"),
                () -> assertTrue(isDisplayed(id("inputAdminPassword")), "Password field should be visible")
        );
    }

    @Test
    @DisplayName("CAA-2: Create admin form shows all required fields")
    void roleSelectionPresent() {
        tap(id("buttonCreateAdminAccount"));
        assertAll(
                () -> assertTrue(isDisplayed(id("inputAdminFirstName")), "First name field should be visible"),
                () -> assertTrue(isDisplayed(id("inputAdminLastName")), "Last name field should be visible"),
                () -> assertTrue(isDisplayed(id("inputAdminEmail")), "Email field should be visible"),
                () -> assertTrue(isDisplayed(id("inputAdminPassword")), "Password field should be visible"),
                () -> assertTrue(isDisplayed(id("inputAdminConfirmPassword")), "Confirm password field should be visible"),
                () -> assertTrue(isDisplayed(id("btnCreateAdminAccount")), "Create button should be visible")
        );
    }

    @Test
    @DisplayName("CAA-3: Valid form submits new admin account")
    void validFormSubmitsNewAdmin() {
        tap(id("buttonCreateAdminAccount"));
        // Every required field must be filled: the form blocks submit on a
        // blank last name and an invalid phone (CreateAdminAccountActivity +
        // Validators.isValidPhoneNumber).
        scrollToField("inputAdminFirstName");
        type(id("inputAdminFirstName"), "New Admin");
        scrollToField("inputAdminLastName");
        type(id("inputAdminLastName"), "Tester");
        scrollToField("inputAdminEmail");
        type(id("inputAdminEmail"), TestConfig.NEW_USER_EMAIL);
        scrollToField("inputAdminPhone");
        type(id("inputAdminPhone"), "+639123456789");
        scrollToField("inputAdminPassword");
        type(id("inputAdminPassword"), TestConfig.ADMIN_PASS);
        scrollToField("inputAdminConfirmPassword");
        type(id("inputAdminConfirmPassword"), TestConfig.ADMIN_PASS);
        scrollToField("btnCreateAdminAccount");
        tap(id("btnCreateAdminAccount"));
        // Success finishes the form back on the account-list activity (the
        // success toast text is backend-driven, so assert the deterministic
        // navigation instead of matching on toast text).
        assertTrue(waitForActivity("AdminAccountManagementActivity"),
                "New admin account creation should complete and return to the account list");
    }

    private void scrollToField(String resourceId) {
        String selector = "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView("
                + "new UiSelector().resourceId(\"" + id(resourceId) + "\"))";
        wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator(selector)));
    }
}
