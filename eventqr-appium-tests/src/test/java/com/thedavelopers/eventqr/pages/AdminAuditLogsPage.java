package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Admin Audit Logs screen (TestFlow: AL-1..AL-6).
 * Layouts: activity_admin_audit_logs.xml, item_admin_audit_log.xml
 */
public class AdminAuditLogsPage extends BaseTest {

    // Filter chips
    private static final String CHIP_ALL = "chipAuditAll";
    private static final String CHIP_APPROVAL = "chipAuditApproval";
    private static final String CHIP_ACCOUNT = "chipAuditAccount";
    private static final String CHIP_SECURITY = "chipAuditSecurity";
    private static final String CHIP_NOTIFICATION = "chipAuditNotification";

    private static final String SWIPE_REFRESH = "swipeRefreshAuditLogs";
    private static final String RECYCLER = "recyclerAuditLogs";
    private static final String LOADING = "progressAuditLoading";
    private static final String PLACEHOLDER = "textAuditPlaceholder";

    // Item elements
    private static final String ITEM_ICON_TILE = "auditIconTile";
    private static final String ITEM_ICON = "imgAuditIcon";
    private static final String ITEM_ACTION_TITLE = "textAuditActionTitle";
    private static final String ITEM_ACTOR_TARGET = "textAuditActorTarget";
    private static final String ITEM_TIMESTAMP = "textAuditTimestamp";

    // Bottom nav
    private static final String NAV_DASHBOARD = "navDashboard";
    private static final String NAV_REQUESTS = "navRequests";
    private static final String NAV_ACCOUNTS = "navAccounts";
    private static final String NAV_LOGS = "navLogs";

    public boolean isAuditLogsVisible() {
        return isDisplayed(id(CHIP_ALL)) && (isDisplayed(id(RECYCLER))
                || isDisplayed(id(PLACEHOLDER)) || isDisplayed(id(NAV_LOGS)));
    }

    public void tapAllFilter() {
        tap(id(CHIP_ALL));
    }

    public void tapApprovalFilter() {
        tap(id(CHIP_APPROVAL));
    }

    public void tapAccountFilter() {
        tap(id(CHIP_ACCOUNT));
    }

    public void tapSecurityFilter() {
        tap(id(CHIP_SECURITY));
    }

    public void tapNotificationFilter() {
        tap(id(CHIP_NOTIFICATION));
    }

    public void refresh() {
        swipeDown();
    }

    public boolean isLoadingDisplayed() {
        return isDisplayed(id(LOADING));
    }

    public boolean isPlaceholderDisplayed() {
        return isDisplayed(id(PLACEHOLDER));
    }

    public boolean isAuditItemDisplayed() {
        return isDisplayed(id(ITEM_ACTION_TITLE));
    }

    public String getItemActionTitle() {
        return getText(id(ITEM_ACTION_TITLE));
    }

    public String getItemActorTarget() {
        return getText(id(ITEM_ACTOR_TARGET));
    }

    public String getItemTimestamp() {
        return getText(id(ITEM_TIMESTAMP));
    }

    public boolean isItemIconDisplayed() {
        return isDisplayed(id(ITEM_ICON));
    }

    public boolean isAuditActionVisible(String action) {
        return isTextDisplayed(action);
    }

    public void tapAuditItem() {
        tap(id(ITEM_ACTION_TITLE));
    }

    public void openDashboardTab() {
        tap(id(NAV_DASHBOARD));
    }

    public void openRequestsTab() {
        tap(id(NAV_REQUESTS));
    }

    public void openAccountsTab() {
        tap(id(NAV_ACCOUNTS));
    }

    public void openLogsTab() {
        tap(id(NAV_LOGS));
    }
}