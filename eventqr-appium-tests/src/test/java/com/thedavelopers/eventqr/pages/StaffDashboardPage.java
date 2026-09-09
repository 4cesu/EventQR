package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

public class StaffDashboardPage extends BaseTest {

    private static final String EVENT_SELECTOR = "spinnerEventSelector";
    private static final String RECENT_SCANS_LIST = "recyclerRecentScans";
    private static final String SCAN_BUTTON = "btnScanQr";
    private static final String BOTTOM_NAV_DASHBOARD = "navDashboard";
    private static final String BOTTOM_NAV_LOGS = "navLogs";

    public boolean isDashboardVisible() {
        return isDisplayed(id("txtWelcome"));
    }

    public void selectEvent() {
        tap(id(EVENT_SELECTOR));
    }

    public void openLogsTab() {
        tap(id(BOTTOM_NAV_LOGS));
    }

    public boolean isRecentScansListVisible() {
        return isDisplayed(id(RECENT_SCANS_LIST));
    }

    public boolean isScanButtonVisible() {
        return isDisplayed(id(SCAN_BUTTON));
    }
}
