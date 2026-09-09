package com.thedavelopers.eventqr.tests.admin;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AdminDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminAccountManagementTest extends BaseTest {

    private AdminDashboardPage dash;

    @BeforeEach
    void loginAsAdminAndOpenAccounts() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
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
        type(id("inputAdminFirstName"), "New Admin");
        type(id("inputAdminEmail"), TestConfig.NEW_USER_EMAIL);
        type(id("inputAdminPassword"), TestConfig.ADMIN_PASS);
        type(id("inputAdminConfirmPassword"), TestConfig.ADMIN_PASS);
        tap(id("btnCreateAdminAccount"));
        assertTrue(isTextDisplayed("created") || isTextDisplayed("success") || isTextDisplayed("Added"),
                "New admin account creation should complete");
    }
}
