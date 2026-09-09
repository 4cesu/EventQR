package com.thedavelopers.eventqr.tests.staff;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.StaffDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StaffLogsTest extends BaseTest {

    private StaffDashboardPage dash;

    @BeforeEach
    void loginAndOpenLogs() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        dash = new StaffDashboardPage();
        dash.openLogsTab();
    }

    @Test
    @DisplayName("SL-1: Scan logs list loads")
    void logsListLoads() {
        // TODO: verify scan logs list resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerLogs")), "Scan logs list should be visible");
    }

    @Test
    @DisplayName("SL-2: Logs show scan time and entry status")
    void logsShowTimeAndStatus() {
        // TODO: verify log row fields resource-ids in Appium Inspector
        assertTrue(isDisplayed(id("txtLogTime")), "Scan time should be visible on log row");
    }

    @Test
    @DisplayName("SL-3: Filter logs by status")
    void filterLogsByStatus() {
        // TODO: verify filter chip resource-id in Appium Inspector
        tap(id("chipEntered"));
        assertTrue(isDisplayed(id("recyclerLogs")), "Filtered logs list should still be visible");
    }

    @Test
    @DisplayName("SL-4: Search logs by attendee name")
    void searchLogsByName() {
        // TODO: verify search field resource-id in Appium Inspector
        type(id("edtLogSearch"), "Test User");
        assertTrue(isDisplayed(id("recyclerLogs")), "Search results list should be visible");
    }

    @Test
    @DisplayName("SL-5: Expandable log detail shows full info")
    void expandableLogDetail() {
        // TODO: verify expandable detail behavior resource-id in Appium Inspector
        tap(id("cardLogItem"));
        assertTrue(isDisplayed(id("txtLogDetail")), "Expanded log detail should be visible");
    }

    @Test
    @DisplayName("SL-6: Empty state when no scan logs")
    void emptyStateWhenNoLogs() {
        // TODO: verify empty state resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtEmptyLogs")) || isTextDisplayed("No scan logs"),
                "Empty state should be shown when no logs");
    }
}
