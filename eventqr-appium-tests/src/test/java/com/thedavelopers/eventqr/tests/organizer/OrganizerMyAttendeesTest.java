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
 * TestFlow 6.10 — My Attendees (AM-1..AM-3).
 * AttendeeManagementActivity (activity_attendee_management.xml) + the attendee
 * search/filter screen. Row flows branch on the empty state
 * (data-dependent: registrations exist only after organizers publish + users
 * register).
 */
public class OrganizerMyAttendeesTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void openAttendees() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapBottomNavLabel("Attendees");
    }

    @Test
    @DisplayName("AM-1: Attendee list with summary counts renders")
    void attendeeListRenders() {
        assertAll(
                () -> assertTrue(isDisplayed(id("txtTotalCount")), "Total count expected"),
                () -> assertTrue(isDisplayed(id("txtCheckedInCount")), "Checked-in count expected"),
                () -> assertTrue(isDisplayed(id("txtNoShowCount")), "No-show count expected"),
                () -> assertTrue(isDisplayed(id("recyclerAttendees")) || isDisplayed(id("txtAttendeesEmpty")),
                        "Attendee list or its empty state expected")
        );
    }

    @Test
    @DisplayName("AM-2: Tapping an attendee opens their details")
    void attendeeOpensDetails() {
        if (findElements(id("txtAttendeeName")).isEmpty()) {
            assertTrue(isDisplayed(id("txtAttendeesEmpty")), "No attendees; empty state renders");
            return;
        }
        findElements(id("txtAttendeeName")).get(0).click();
        assertTrue(isTextDisplayed("Attendee Details") || isDisplayed(id("txtDetailName")),
                "Attendee details screen should open");
    }

    @Test
    @DisplayName("AM-3: Filter opens the attendee search screen")
    void filterOpensSearch() {
        tap(id("btnFilter"));
        assertAll(
                () -> assertTrue(isDisplayed(id("txtSearchTitle")), "Search title expected"),
                () -> assertTrue(isDisplayed(id("edtSearchAttendees")), "Search field expected"),
                () -> assertTrue(isDisplayed(id("chipAll")), "All chip expected"),
                () -> assertTrue(isDisplayed(id("chipCheckedIn")) || isDisplayed(id("chipRegistered")),
                        "Status chips expected")
        );
    }
}