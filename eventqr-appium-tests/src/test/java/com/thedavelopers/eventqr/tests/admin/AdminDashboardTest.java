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
 * TestFlow 7.1 — Admin Dashboard (AD-1..AD-2).
 * Stats cards + pending-approvals alert banner (cardPendingAlert, hidden when
 * zero pending requests) + portal switcher + bottom navigation.
 */
public class AdminDashboardTest extends BaseTest {

    private AdminDashboardPage dash;

    @BeforeEach
    void loginAsAdmin() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        dash = new AdminDashboardPage();
    }

    @Test
    @DisplayName("AD-1: Dashboard stats and pending alert navigate to requests")
    void statsAndPendingAlert() {
        assertTrue(dash.isDashboardVisible(), "Admin dashboard should be visible");
        assertAll(
                () -> assertTrue(isDisplayed(id("textPendingRequestsValue")), "Pending requests stat"),
                () -> assertTrue(isDisplayed(id("textTotalAccountsValue")), "Total accounts stat"),
                () -> assertTrue(isDisplayed(id("textActiveEventsValue")), "Active events stat"),
                () -> assertTrue(isDisplayed(id("textAuditLogsValue")), "Audit logs stat")
        );
        if (dash.isPendingAlertVisible()) {
            assertTrue(dash.getPendingAlertText().contains("pending"),
                    "Alert banner should mention pending requests");
            dash.tapPendingAlert();
            assertTrue(isDisplayed(id("recyclerRequests")),
                    "Alert banner should open the requests list");
        }
        // With zero pending requests the banner is hidden by design — the
        // stats contract above still holds.
    }

    @Test
    @DisplayName("AD-2: Portal switcher and bottom navigation render")
    void portalSwitcherAndBottomNav() {
        assertAll(
                () -> assertTrue(isDisplayed(id("portalSwitcherChip")), "Portal switcher chip expected"),
                () -> assertTrue(isDisplayed(id("navDashboard")), "Dashboard nav item"),
                () -> assertTrue(isDisplayed(id("navRequests")), "Requests nav item"),
                () -> assertTrue(isDisplayed(id("navAccounts")), "Accounts nav item"),
                () -> assertTrue(isDisplayed(id("navLogs")), "Logs nav item")
        );
        dash.tapPortalSwitcher();
        assertAll(
                () -> assertTrue(isTextDisplayed("Attendee Portal"), "Portal sheet lists Attendee Portal"),
                () -> assertTrue(isTextDisplayed("Admin Portal"), "Portal sheet lists Admin Portal"),
                () -> assertTrue(isDisplayed(id("currentPortalBadge")), "Current portal badge expected")
        );
        pressBack();
        assertTrue(dash.isDashboardVisible(), "Dismissing the sheet returns to the dashboard");
    }
}