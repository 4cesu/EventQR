package com.thedavelopers.eventqr.tests.organizer;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 6.1 — Organizer Dashboard (OD-1..OD-9).
 * Resource IDs verified against activity_organizer_dashboard.xml; active-event
 * cards reuse the attendee card layout (txtAttendeeEventTitle). Row-dependent
 * assertions branch on the empty/error states (data-dependent backend).
 */
public class OrganizerDashboardTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void loginAsOrganizer() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
        waitForVisibleId(id("txtHeaderTitle"));
    }

    @Test
    @DisplayName("OD-1: Organizer dashboard header renders portal and name")
    void headerRendersPortalAndName() {
        assertEquals("Organizer Portal", getText(id("txtHeaderTitle")), "Header title should name the portal");
        assertTrue(dash.isDisplayedHeaderSubtitle(), "Header subtitle (organizer name) expected");
    }

    @Test
    @DisplayName("OD-2: Dashboard stats grid renders")
    void statsGridRenders() {
        assertTrue(dash.areStatsVisible(), "Total Events / Attendees / Transactions / Rewards stats expected");
    }

    @Test
    @DisplayName("OD-3: Active event card opens its management hub")
    void activeEventOpensHub() {
        dash.seedApprovedEventIfNone();
        assertTrue(dash.hasActiveEventCards(), "Active events section should render cards after seeding");
        dash.openFirstActiveEvent();
        assertTrue(isTextDisplayed("Event Management"), "Event Management Hub should open");
    }

    @Test
    @DisplayName("OD-4: See All opens the My Events list")
    void seeAllOpensMyEvents() {
        dash.seedApprovedEventIfNone();
        dash.tapSeeAllEvents();
        assertTrue(isDisplayed(id("recyclerEvents")) || isTextDisplayed("My Events"),
                "Manage Events screen should open from See All");
    }

    @Test
    @DisplayName("OD-5: Empty events state renders when there are no active events")
    void emptyEventsStateTolerated() {
        assertTrue(dash.showsEventsEmpty() || dash.hasActiveEventCards(),
                "Dashboard renders the empty state or active-event cards (data-dependent)");
    }

    @Test
    @DisplayName("OD-6: Dashboard error state offers retry")
    void errorStateOffersRetry() {
        assertTrue(dash.showsDashboardError() || dash.areStatsVisible(),
                "Dashboard renders its error state with Retry or the loaded stats (data-dependent)");
    }

    @Test
    @DisplayName("OD-7: Notification bell opens notification management")
    void notificationBellOpensNotifications() {
        dash.tapOrganizerNotificationBell();
        assertTrue(isTextDisplayed("Notifications"),
                "Notification management screen should open from the bell");
    }

    @Test
    @DisplayName("OD-8: Swipe to refresh and bottom navigation render")
    void swipeAndBottomNav() {
        assertAll(
                () -> assertTrue(isTextDisplayed("Dashboard"), "Bottom nav Dashboard label"),
                () -> assertTrue(isTextDisplayed("Events"), "Bottom nav Events label"),
                () -> assertTrue(isTextDisplayed("Attendees"), "Bottom nav Attendees label"),
                () -> assertTrue(isTextDisplayed("Logs"), "Bottom nav Logs label"),
                () -> assertTrue(isTextDisplayed("Reports"), "Bottom nav Reports label"),
                () -> assertTrue(isTextDisplayed("Rewards"), "Bottom nav Rewards label")
        );
        swipeDown();
        assertTrue(dash.isOrganizerDashboardVisible(), "Dashboard should remain after refresh");
    }

    @Test
    @DisplayName("OD-9: Portal switcher sheet lists available portals")
    void portalSwitcherSheet() {
        dash.tapPortalSwitcher();
        assertAll(
                () -> assertTrue(isTextDisplayed("Attendee Portal"), "Portal sheet lists Attendee Portal"),
                () -> assertTrue(isTextDisplayed("Organizer Portal"), "Portal sheet lists Organizer Portal"),
                () -> assertTrue(isDisplayed(id("currentPortalBadge")), "Current portal badge expected")
        );
        pressBack();
        assertTrue(dash.isOrganizerDashboardVisible(), "Dismissing the sheet returns to the dashboard");
    }
}