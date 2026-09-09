package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

public class AdminDashboardPage extends BaseTest {

    private static final String PORTAL_SWITCHER = "portalSwitcherChip";
    private static final String PENDING_REQUESTS_VALUE = "textPendingRequestsValue";
    private static final String TOTAL_ACCOUNTS_VALUE = "textTotalAccountsValue";
    private static final String ACTIVE_EVENTS_VALUE = "textActiveEventsValue";
    private static final String AUDIT_LOGS_VALUE = "textAuditLogsValue";
    private static final String NOTIFICATION_MANAGEMENT_CARD = "cardAdminNotifications";
    private static final String BOTTOM_NAV_REQUESTS = "navRequests";
    private static final String BOTTOM_NAV_ACCOUNTS = "navAccounts";
    private static final String BOTTOM_NAV_LOGS = "navLogs";

    public boolean isDashboardVisible() {
        return isDisplayed(id("textAdminPortalTitle"));
    }

    public void tapPortalSwitcher() {
        tap(id(PORTAL_SWITCHER));
    }

    public String getPortalTitle() {
        return getText(id("textAdminPortalTitle"));
    }

    public String getPendingRequestsValue() {
        return getText(id(PENDING_REQUESTS_VALUE));
    }

    public String getTotalAccountsValue() {
        return getText(id(TOTAL_ACCOUNTS_VALUE));
    }

    public String getActiveEventsValue() {
        return getText(id(ACTIVE_EVENTS_VALUE));
    }

    public String getAuditLogsValue() {
        return getText(id(AUDIT_LOGS_VALUE));
    }

    public void openRequestsTab() {
        tap(id(BOTTOM_NAV_REQUESTS));
    }

    public void openAccountsTab() {
        tap(id(BOTTOM_NAV_ACCOUNTS));
    }

    public void openLogsTab() {
        tap(id(BOTTOM_NAV_LOGS));
    }

    public void tapNotificationManagement() {
        tap(id(NOTIFICATION_MANAGEMENT_CARD));
    }
}
