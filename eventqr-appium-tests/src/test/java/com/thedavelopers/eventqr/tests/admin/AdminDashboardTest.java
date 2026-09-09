package com.thedavelopers.eventqr.tests.admin;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AdminDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminDashboardTest extends BaseTest {

    private AdminDashboardPage dash;

    @BeforeEach
    void loginAsAdminAndOpenDashboard() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        dash = new AdminDashboardPage();
    }

    @Test
    @DisplayName("AD-1: Admin dashboard loads with portal title and stats")
    void dashboardLoadsWithStats() {
        assertTrue(dash.isDashboardVisible(), "Admin dashboard should be visible");
        assertAll(
                () -> assertTrue(isDisplayed(id("textPendingRequestsValue")), "Pending requests stat should be visible"),
                () -> assertTrue(isDisplayed(id("textTotalAccountsValue")), "Total accounts stat should be visible"),
                () -> assertTrue(isDisplayed(id("textActiveEventsValue")), "Active events stat should be visible"),
                () -> assertTrue(isDisplayed(id("textAuditLogsValue")), "Audit logs stat should be visible")
        );
    }

    @Test
    @DisplayName("AD-2: Admin dashboard summary values non-negative")
    void dashboardSummaryValuesNonNegative() {
        assertTrue(Integer.parseInt(dash.getPendingRequestsValue()) >= 0, "Pending requests should be non-negative");
        assertTrue(Integer.parseInt(dash.getTotalAccountsValue()) >= 0, "Total accounts should be non-negative");
    }
}
