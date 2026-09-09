package com.thedavelopers.eventqr.tests.admin;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AdminDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 7.5 — Admin Notification Management (A-NOT-1).
 * AdminNotificationManagementActivity is a shell (bottom navigation only);
 * assert the screen renders with its four admin nav items.
 */
public class AdminNotificationManagementTest extends BaseTest {

    @BeforeEach
    void openNotificationManagement() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        new AdminDashboardPage().tapNotificationManagement();
    }

    @Test
    @DisplayName("A-NOT-1: Notification management shell renders with admin bottom nav")
    void notificationManagementRenders() {
        assertAll(
                () -> assertTrue(isDisplayed(id("navDashboard")), "Dashboard nav item"),
                () -> assertTrue(isDisplayed(id("navRequests")), "Requests nav item"),
                () -> assertTrue(isDisplayed(id("navAccounts")), "Accounts nav item"),
                () -> assertTrue(isDisplayed(id("navLogs")), "Logs nav item")
        );
    }
}