package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Page-object for {@code StaffDashboardActivity} (activity_staff_dashboard.xml).
 *
 * <p>All resource IDs verified against the EventQRMobile sources. The staff
 * dashboard has no event spinner and no QR button — the header shows the
 * staff name, tiles open the Scanner / Assigned-Events screens, the bell
 * opens notifications, and the portal-switcher chip + 4-item bottom nav
 * (Dashboard / Scan / Events / Logs) drive the rest of the staff portal.</p>
 */
public class StaffDashboardPage extends BaseTest {

    // Header
    private static final String TXT_PORTAL_LABEL    = "txtPortalLabel";
    private static final String TXT_STAFF_NAME      = "txtStaffName";

    // Tiles
    private static final String TXT_SCANS_TODAY     = "txtScansToday";
    private static final String TXT_CHECKINS_TODAY  = "txtCheckinsToday";

    // Notifications bell
    private static final String BTN_NOTIFICATION    = "btnNotification";
    private static final String VIEW_NOTIFICATION_DOT = "viewNotificationDot";

    // Recent scans section
    private static final String RECENT_SCANS_LIST   = "recyclerRecentScans";
    private static final String RECENT_SCANS_EMPTY  = "txtRecentScansEmpty";

    // Portal switcher
    private static final String PORTAL_SWITCHER_CHIP = "portalSwitcherChip";
    private static final String TXT_CURRENT_PORTAL   = "txtCurrentPortal";

    // Bottom navigation
    private static final String NAV_DASHBOARD = "navDashboard";
    private static final String NAV_SCANNER   = "navScanner";
    private static final String NAV_EVENTS    = "navEvents";
    private static final String NAV_LOGS      = "navLogs";

    public boolean isDashboardVisible() {
        return isDisplayed(id(TXT_STAFF_NAME));
    }

    public boolean isStaffHeaderVisible() {
        return isDisplayed(id(TXT_PORTAL_LABEL)) && isDisplayed(id(TXT_STAFF_NAME));
    }

    public boolean isScansTodayVisible() {
        return isDisplayed(id(TXT_SCANS_TODAY));
    }

    public boolean isCheckinsTodayVisible() {
        return isDisplayed(id(TXT_CHECKINS_TODAY));
    }

    /** Tiles */
    public void openScanner() {
        tap(id(TXT_SCANS_TODAY));
    }

    public void openAssignedEvents() {
        tap(id(TXT_CHECKINS_TODAY));
    }

    /** Notifications */
    public void openNotifications() {
        tap(id(BTN_NOTIFICATION));
    }

    public boolean isNotificationBellVisible() {
        return isDisplayed(id(BTN_NOTIFICATION));
    }

    /** Recent scans (content OR empty-state — data dependent) */
    public boolean isRecentScansListVisible() {
        return isDisplayed(id(RECENT_SCANS_LIST));
    }

    public boolean isRecentScansEmptyVisible() {
        return isDisplayed(id(RECENT_SCANS_EMPTY));
    }

    public boolean isRecentScansSectionLoaded() {
        return isRecentScansListVisible() || isRecentScansEmptyVisible();
    }

    /** Bottom navigation */
    public void openLogsTab() {
        tap(id(NAV_LOGS));
    }

    public void openScannerTab() {
        tap(id(NAV_SCANNER));
    }

    public void openEventsTab() {
        tap(id(NAV_EVENTS));
    }

    public boolean isBottomNavVisible() {
        return isDisplayed(id(NAV_DASHBOARD)) && isDisplayed(id(NAV_SCANNER))
                && isDisplayed(id(NAV_EVENTS)) && isDisplayed(id(NAV_LOGS));
    }

    public boolean isBottomNavLabelVisible(String label) {
        return isTextDisplayed(label);
    }

    /** Portal switcher */
    public void tapPortalSwitcher() {
        tap(id(PORTAL_SWITCHER_CHIP));
    }

    public boolean isCurrentPortalTextVisible() {
        return isDisplayed(id(TXT_CURRENT_PORTAL));
    }
}