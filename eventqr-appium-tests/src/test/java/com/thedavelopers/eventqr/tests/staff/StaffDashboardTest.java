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
 * TestFlow 5.1 — Staff Dashboard (SD-1..SD-7).
 * Resource IDs verified against activity_staff_dashboard.xml.
 */
public class StaffDashboardTest extends BaseTest {

    private StaffDashboardPage dash;

    @BeforeEach
    void loginAsStaff() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        dash = new StaffDashboardPage();
    }

    @Test
    @DisplayName("SD-1: Staff dashboard restricts access to staff accounts")
    void staffOnlyGuard() {
        restartAppToLogin();
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("btnNotificationsHub"));
        startActivity("com.thedavelopers.eventqr.features.staff.StaffDashboardActivity", null);
        assertTrue(isToastDisplayed("Access Denied: Staff only")
                        || isDisplayed(id("btnNotificationsHub")),
                "Non-staff must be blocked from the staff dashboard (toast or stay on user dashboard)");
    }

    @Test
    @DisplayName("SD-2: Scans Today tile opens the scanner")
    void scansTileOpensScanner() {
        dash.openScanner();
        assertTrue(isDisplayed(id("btnSubmitScan")), "Scanner screen should open from the Scans tile");
        assertTrue(isDisplayed(id("cardScannerEvent")) || isDisplayed(id("txtScannerEmptyState")),
                "Scanner event context expected");
    }

    @Test
    @DisplayName("SD-3: Check-ins Today tile opens assigned events")
    void checkinsTileOpensAssignedEvents() {
        dash.openAssignedEvents();
        assertTrue(isDisplayed(id("recyclerAssignedEvents")) || isDisplayed(id("txtAssignedEventsEmpty")),
                "Assigned events screen should open from the Check-ins tile");
    }

    @Test
    @DisplayName("SD-4: Recent scans section renders (list or empty state)")
    void recentScansSectionRenders() {
        assertTrue(dash.isRecentScansSectionLoaded(),
                "Recent scans should render a list or its empty state");
    }

    @Test
    @DisplayName("SD-5: Notification bell opens notifications")
    void notificationBellOpensNotifications() {
        dash.openNotifications();
        assertTrue(isDisplayed(id("recyclerNotifications")) || isDisplayed(id("txtNotificationsEmpty")),
                "Notifications screen should open from the bell");
    }

    @Test
    @DisplayName("SD-6: Swipe to refresh reloads the dashboard")
    void swipeToRefresh() {
        swipeDown();
        assertTrue(dash.isDashboardVisible(), "Dashboard should remain visible after refresh");
        assertTrue(dash.isRecentScansSectionLoaded() || isDisplayed(id("skeletonLoading")),
                "Recent scans section should reload after pull-to-refresh");
    }

    @Test
    @DisplayName("SD-7: Portal switcher and bottom navigation render")
    void portalSwitcherAndBottomNav() {
        assertAll(
                () -> assertTrue(isTextDisplayed("Dashboard"), "Bottom nav Dashboard label"),
                () -> assertTrue(isTextDisplayed("Scan"), "Bottom nav Scan label"),
                () -> assertTrue(isTextDisplayed("Events"), "Bottom nav Events label"),
                () -> assertTrue(isTextDisplayed("Logs"), "Bottom nav Logs label")
        );
        dash.tapPortalSwitcher();
        assertAll(
                () -> assertTrue(isTextDisplayed("Attendee Portal"), "Portal sheet lists Attendee Portal"),
                () -> assertTrue(isTextDisplayed("Staff Portal"), "Portal sheet lists Staff Portal"),
                () -> assertTrue(isDisplayed(id("currentPortalBadge")), "Portal sheet marks the current portal")
        );
        pressBack();
        assertTrue(dash.isDashboardVisible(), "Dismissing the sheet returns to the dashboard");
    }
}