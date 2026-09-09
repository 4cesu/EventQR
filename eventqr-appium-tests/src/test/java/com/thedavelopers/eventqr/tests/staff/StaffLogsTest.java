package com.thedavelopers.eventqr.tests.staff;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.StaffDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 5.7 — Staff Transactions / Logs (STX-1..STX-3).
 * The staff Logs tab opens StaffTransactionsActivity
 * (activity_staff_transaction_logs.xml).
 */
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
    @DisplayName("STX-1: Transaction logs screen loads from the Logs tab")
    void logsScreenLoads() {
        assertTrue(isDisplayed(id("cardStaffTransactionsEvent")),
                "Logs screen should render the event context card");
        assertTrue(isDisplayed(id("recyclerStaffTransactions"))
                        || isDisplayed(id("txtStaffTransactionsEmptyState")),
                "Transaction list or its empty state should render");
    }

    @Test
    @DisplayName("STX-2: Swipe to refresh reloads the logs")
    void swipeToRefreshLogs() {
        swipeDown();
        assertAll(
                () -> assertTrue(isDisplayed(id("cardStaffTransactionsEvent")),
                        "Event context should remain after refresh"),
                () -> assertTrue(isDisplayed(id("recyclerStaffTransactions"))
                                || isDisplayed(id("txtStaffTransactionsEmptyState")),
                        "Logs should reload after pull-to-refresh")
        );
    }

    @Test
    @DisplayName("STX-3: Logs opened from the scanner nav keep the event context")
    void logsFromScannerKeepEventContext() {
        // From the scanner screen, the Logs nav item carries the selected
        // event id into StaffTransactionsActivity (configureStaffBottomNav).
        dash.openScannerTab();
        assertTrue(isDisplayed(id("btnSubmitScan"))
                        || isDisplayed(id("txtScannerEmptyState")),
                "Scanner screen should be visible before switching to Logs");
        dash.openLogsTab();
        assertTrue(isDisplayed(id("cardStaffTransactionsEvent")),
                "Logs screen should open with the event context card");
    }
}