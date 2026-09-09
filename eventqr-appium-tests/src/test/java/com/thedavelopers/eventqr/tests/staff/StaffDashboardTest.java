package com.thedavelopers.eventqr.tests.staff;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.StaffDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StaffDashboardTest extends BaseTest {

    private StaffDashboardPage dash;

    @BeforeEach
    void loginAsStaffAndOpenDashboard() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        dash = new StaffDashboardPage();
    }

    @Test
    @DisplayName("SD-1: Staff dashboard loads with event selector")
    void dashboardLoadsWithEventSelector() {
        assertTrue(dash.isDashboardVisible(), "Staff dashboard should be visible");
        // TODO: verify event selector resource-id in Appium Inspector
        assertTrue(isDisplayed(id("spinnerEventSelector")), "Event selector should be visible");
    }

    @Test
    @DisplayName("SD-2: Event selector lists assigned events")
    void eventSelectorListsAssignedEvents() {
        dash.selectEvent();
        // TODO: verify dropdown option text in Appium Inspector
        assertTrue(isTextDisplayed("Select Event") || isDisplayed(id("event_option")),
                "Event dropdown should show options");
    }

    @Test
    @DisplayName("SD-3: Recent scans list is displayed")
    void recentScansListDisplayed() {
        assertTrue(dash.isRecentScansListVisible(), "Recent scans list should be visible");
    }

    @Test
    @DisplayName("SD-4: Scan button is visible")
    void scanButtonVisible() {
        assertTrue(dash.isScanButtonVisible(), "Scan QR button should be visible");
    }

    @Test
    @DisplayName("SD-5: Dashboard shows staff summary stats")
    void dashboardShowsSummaryStats() {
        // TODO: verify summary stats resource-ids in Appium Inspector
        assertTrue(isDisplayed(id("txtTotalScans")), "Total scans stat should be visible");
    }

    @Test
    @DisplayName("SD-6: Swipe down refreshes dashboard")
    void swipeDownRefreshes() {
        swipeDown();
        assertTrue(dash.isDashboardVisible(), "Dashboard should remain visible after refresh");
    }
}
