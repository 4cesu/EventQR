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
 * TestFlow 5.3 — Staff Assigned Events (SAE-1..SAE-6).
 * activity_staff_assigned_events.xml + item_staff_assigned_event.xml.
 * Rows exist only when the staff account is assigned to events, so row-based
 * flows branch on the empty state (data-dependent backend).
 */
public class StaffAssignedEventsTest extends BaseTest {

    private StaffDashboardPage dash;

    @BeforeEach
    void loginAsStaffAndOpenAssignedEvents() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        dash = new StaffDashboardPage();
        dash.openAssignedEvents();
    }

    @Test
    @DisplayName("SAE-1: Assigned events screen restricts access to staff accounts")
    void staffOnlyGuard() {
        restartAppToLogin();
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("btnNotificationsHub"));
        startActivity("com.thedavelopers.eventqr.features.staff.StaffAssignedEventsActivity", null);
        assertTrue(isToastDisplayed("Access Denied: Staff only")
                        || isDisplayed(id("btnNotificationsHub")),
                "Non-staff must be blocked from the assigned events screen");
    }

    @Test
    @DisplayName("SAE-2: Assigned events list or empty state renders")
    void assignedEventsListRenders() {
        assertTrue(isDisplayed(id("recyclerAssignedEvents"))
                        || isDisplayed(id("txtAssignedEventsEmpty")),
                "Assigned events should render a list or its empty state");
    }

    @Test
    @DisplayName("SAE-3: Event rows offer Scan and Attendees actions")
    void eventRowsOfferActions() {
        if (isDisplayed(id("txtStaffEventTitle"))) {
            assertAll(
                    () -> assertTrue(isDisplayed(id("btnScan")), "Row Scan action expected"),
                    () -> assertTrue(isDisplayed(id("btnAttendees")), "Row Attendees action expected")
            );
        } else {
            assertTrue(isDisplayed(id("txtAssignedEventsEmpty")),
                    "Without assigned events the empty state renders instead");
        }
    }

    @Test
    @DisplayName("SAE-4: Scan action opens the scanner for that event")
    void scanActionOpensScannerForEvent() {
        if (findElements(id("btnScan")).isEmpty()) {
            assertTrue(isDisplayed(id("txtAssignedEventsEmpty")), "No events available to scan");
            return;
        }
        findElements(id("btnScan")).get(0).click();
        assertAll(
                () -> assertTrue(isDisplayed(id("btnSubmitScan")), "Scanner should open"),
                () -> assertTrue(isDisplayed(id("cardScannerEvent")) || isDisplayed(id("txtScannerEmptyState")),
                        "Scanner should carry event context")
        );
    }

    @Test
    @DisplayName("SAE-5: Attendees action opens registrations for that event")
    void attendeesActionOpensRegistrations() {
        if (findElements(id("btnAttendees")).isEmpty()) {
            assertTrue(isDisplayed(id("txtAssignedEventsEmpty")), "No events available to open");
            return;
        }
        findElements(id("btnAttendees")).get(0).click();
        assertTrue(isDisplayed(id("edtRegistrationSearch"))
                        || isDisplayed(id("recyclerEventRegistrations"))
                        || isDisplayed(id("txtEventRegistrationsEventTitle")),
                "Registrations screen should open from the Attendees action");
    }

    @Test
    @DisplayName("SAE-6: Swipe to refresh reloads the list")
    void swipeToRefreshReloads() {
        swipeDown();
        assertTrue(isDisplayed(id("recyclerAssignedEvents"))
                        || isDisplayed(id("txtAssignedEventsEmpty"))
                        || isDisplayed(id("skeletonLoading")),
                "Screen should remain after pull-to-refresh");
    }
}