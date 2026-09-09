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
        // TODO: verify account list resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerAccounts")), "Account list should be visible");
    }

    @Test
    @DisplayName("AAM-2: Filter chips displayed for account roles")
    void filterChipsDisplayed() {
        // TODO: verify role filter chip resource-ids in Appium Inspector
        assertAll(
                () -> assertTrue(isDisplayed(id("chipAttendee")), "Attendee chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipOrganizer")), "Organizer chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipAdmin")), "Admin chip should be visible")
        );
    }

    @Test
    @DisplayName("AAM-3: Search accounts by name or email")
    void searchAccounts() {
        // TODO: verify search field resource-id in Appium Inspector
        type(id("edtAccountSearch"), "Test");
        assertTrue(isDisplayed(id("recyclerAccounts")), "Search results list should be visible");
    }

    @Test
    @DisplayName("AAM-4: Swipe down refreshes account list")
    void swipeDownRefreshes() {
        swipeDown();
        assertTrue(isDisplayed(id("recyclerAccounts")), "Account list should remain after refresh");
    }

    @Test
    @DisplayName("CAA-1: Create admin account form loads")
    void createAdminFormLoads() {
        // TODO: verify create admin account button resource-id in Appium Inspector
        tap(id("buttonCreateAdminAccount"));
        assertAll(
                () -> assertTrue(isDisplayed(id("edtFirstName")), "First name field should be visible"),
                () -> assertTrue(isDisplayed(id("edtEmail")), "Email field should be visible"),
                () -> assertTrue(isDisplayed(id("edtPassword")), "Password field should be visible")
        );
    }

    @Test
    @DisplayName("CAA-2: Role selection option present")
    void roleSelectionPresent() {
        // TODO: verify role dropdown resource-id in Appium Inspector
        tap(id("buttonCreateAdminAccount"));
        assertTrue(isDisplayed(id("spinnerRole")), "Role selection should be present");
    }

    @Test
    @DisplayName("CAA-3: Valid form submits new admin account")
    void validFormSubmitsNewAdmin() {
        // TODO: verify create account flow resource-ids in Appium Inspector
        tap(id("buttonCreateAdminAccount"));
        type(id("edtFirstName"), "New Admin");
        type(id("edtEmail"), TestConfig.NEW_USER_EMAIL);
        type(id("edtPassword"), TestConfig.ADMIN_PASS);
        tap(id("btnSubmitAdmin"));
        assertTrue(isTextDisplayed("created") || isTextDisplayed("success") || isTextDisplayed("Added"),
                "New admin account creation should complete");
    }
}
