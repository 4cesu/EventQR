package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Admin Notification Management screen (TestFlow: NM-1..NM-3).
 * Layout: activity_admin_notification_management.xml (shell screen — the
 * screen currently shows a placeholder; template management, compose form and
 * recipient selector are not part of the layout yet).
 */
public class AdminNotificationManagementPage extends BaseTest {

    // Shell placeholder text
    private static final String PLACEHOLDER_TEXT =
            "Admin notification management is not configured yet.";

    // Bottom nav
    private static final String NAV_DASHBOARD = "navDashboard";
    private static final String NAV_REQUESTS = "navRequests";
    private static final String NAV_ACCOUNTS = "navAccounts";
    private static final String NAV_LOGS = "navLogs";

    // Planned UI (not yet in layout — verified ids pending; conventions only)
    private static final String TEMPLATE_LIST = "recyclerNotificationTemplates";
    private static final String TITLE_FIELD = "edtNotificationTitle";
    private static final String BODY_FIELD = "edtNotificationBody";
    private static final String RECIPIENT_SELECTOR = "spnRecipientSelector";
    private static final String SEND_BUTTON = "btnSendNotification";

    public boolean isNotificationManagementVisible() {
        return isDisplayed(id(NAV_ACCOUNTS)) && isDisplayed(id(NAV_LOGS));
    }

    public boolean isPlaceholderDisplayed() {
        return isTextDisplayed(PLACEHOLDER_TEXT);
    }

    public boolean isTemplateListDisplayed() {
        // TODO: verify resource-id in Appium Inspector (not yet in layout)
        return isDisplayed(id(TEMPLATE_LIST));
    }

    public boolean isComposeFormDisplayed() {
        // TODO: verify resource-id in Appium Inspector (not yet in layout)
        return isDisplayed(id(TITLE_FIELD)) && isDisplayed(id(BODY_FIELD));
    }

    public void enterTitle(String title) {
        // TODO: verify resource-id in Appium Inspector (not yet in layout)
        type(id(TITLE_FIELD), title);
    }

    public void enterBody(String body) {
        // TODO: verify resource-id in Appium Inspector (not yet in layout)
        type(id(BODY_FIELD), body);
    }

    public void pickRecipientFilter(String filter) {
        // TODO: verify resource-id in Appium Inspector (not yet in layout)
        tap(id(RECIPIENT_SELECTOR));
        tapByText(filter);
    }

    public void tapSend() {
        // TODO: verify resource-id in Appium Inspector (not yet in layout)
        tap(id(SEND_BUTTON));
    }

    public boolean isSendConfirmationVisible() {
        return isTextDisplayed("Notification sent");
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