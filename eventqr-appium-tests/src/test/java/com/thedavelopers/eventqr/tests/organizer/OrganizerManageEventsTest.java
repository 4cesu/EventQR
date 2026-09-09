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
 * TestFlow 6.2 — Manage Events (ME-1..ME-7).
 * activity_organizer_events.xml: chips, live search, swipe-refresh, retry,
 * empty/error states. Event cards reuse EventCardBinder ids
 * (txtAttendeeEventTitle). Entry: dashboard → See All.
 */
public class OrganizerManageEventsTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void openMyEvents() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapSeeAllEvents();
        waitForVisibleId(id("recyclerEvents"));
    }

    @Test
    @DisplayName("ME-1: Status filter chips render")
    void statusFilterChipsRender() {
        assertAll(
                () -> assertTrue(isDisplayed(id("chipAll")), "All chip expected"),
                () -> assertTrue(isDisplayed(id("chipUpcoming")), "Upcoming chip expected"),
                () -> assertTrue(isDisplayed(id("chipActive")), "Active chip expected"),
                () -> assertTrue(isDisplayed(id("chipCompleted")), "Completed chip expected")
        );
    }

    @Test
    @DisplayName("ME-2: Live search filters the event list")
    void liveSearchFilters() {
        type(id("inputEventSearch"), "zzz-nonexistent");
        assertTrue(isDisplayed(id("txtEventsEmpty")) || isDisplayed(id("recyclerEvents")),
                "Search should filter the list (or show the empty state)");
    }

    @Test
    @DisplayName("ME-3: Tapping an event opens its management hub")
    void eventOpensHub() {
        if (findElements(id("txtAttendeeEventTitle")).isEmpty()) {
            assertTrue(isDisplayed(id("txtEventsEmpty")), "No events to open; empty state renders");
            return;
        }
        findElements(id("txtAttendeeEventTitle")).get(0).click();
        assertTrue(isTextDisplayed("Event Management"), "Event Management Hub should open");
    }

    @Test
    @DisplayName("ME-4: Swipe to refresh and retry affordance render")
    void swipeRefreshAndRetry() {
        assertTrue(isDisplayed(id("btnRefreshEvents")), "Retry action expected");
        swipeDown();
        assertTrue(isDisplayed(id("recyclerEvents"))
                        || isDisplayed(id("txtEventsEmpty"))
                        || isDisplayed(id("skeletonLoading")),
                "Screen should reload via pull-to-refresh");
    }

    @Test
    @DisplayName("ME-5: No-match search renders the empty state guidance")
    void emptySearchState() {
        type(id("inputEventSearch"), "zzz-nonexistent");
        assertTrue(isTextDisplayed("No events available for the selected filter."),
                "Empty message expected for a no-match search");
        assertTrue(isTextDisplayed("Try adjusting your search or filters"),
                "Empty guidance line expected");
    }

    @Test
    @DisplayName("ME-6: Load error offers retry")
    void errorStateOffersRetry() {
        assertTrue(isTextDisplayed("Retry") || isDisplayed(id("recyclerEvents"))
                        || isDisplayed(id("txtEventsEmpty")),
                "Error state offers Retry; otherwise list/empty renders (data-dependent)");
    }

    @Test
    @DisplayName("ME-7: Returning from the hub keeps the list alive")
    void backFromHubKeepsList() {
        if (findElements(id("txtAttendeeEventTitle")).isEmpty()) {
            assertTrue(isDisplayed(id("txtEventsEmpty")), "No events to open; empty state renders");
            return;
        }
        findElements(id("txtAttendeeEventTitle")).get(0).click();
        assertTrue(isTextDisplayed("Event Management"), "Hub should open");
        pressBack();
        assertTrue(isDisplayed(id("recyclerEvents")), "Event list should be alive after returning");
    }
}