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
 * TestFlow 6.3 — Event Management Hub (EMH-1..EMH-6).
 * Programmatic screen; all assertions text-based. Entry: dashboard → See All
 * → first event.
 */
public class OrganizerEventManagementHubTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void openHub() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapSeeAllEvents();
        waitForVisibleId(id("recyclerEvents"));
        findElements(id("txtAttendeeEventTitle")).get(0).click();
        waitForText("Event Management");
    }

    @Test
    @DisplayName("EMH-1: Hub renders title and lifecycle status")
    void hubRendersTitleAndStatus() {
        assertTrue(isTextDisplayed("Event Management"), "Hub title expected");
        assertTrue(isTextDisplayed("Upcoming") || isTextDisplayed("Active") || isTextDisplayed("Completed"),
                "Lifecycle status pill expected");
    }

    @Test
    @DisplayName("EMH-2: Registration summary renders")
    void registrationSummaryRenders() {
        assertAll(
                () -> assertTrue(isTextDisplayed("Registered"), "Registered summary expected"),
                () -> assertTrue(isTextDisplayed("Capacity"), "Capacity summary expected"),
                () -> assertTrue(isTextDisplayed("Available"), "Available summary expected")
        );
    }

    @Test
    @DisplayName("EMH-3: Edit/View details row renders")
    void detailsEntryRenders() {
        assertTrue(isTextDisplayed("Edit Event Details") || isTextDisplayed("View Event Details"),
                "Details row expected (edit for upcoming, view for others)");
    }

    @Test
    @DisplayName("EMH-4: Management menu rows render")
    void menuRowsRender() {
        assertAll(
                () -> assertTrue(isTextDisplayed("Staff Assignment"), "Staff Assignment row expected"),
                () -> assertTrue(isTextDisplayed("Scan Purposes"), "Scan Purposes row expected"),
                () -> assertTrue(isTextDisplayed("Transaction Rules"), "Transaction Rules row expected"),
                () -> assertTrue(isTextDisplayed("ID Display Settings"), "ID Display Settings row expected")
        );
    }

    @Test
    @DisplayName("EMH-5: Unknown event id shows the missing-event state")
    void unknownEventMissingState() {
        startActivity("com.thedavelopers.eventqr.features.organizer.events.EventManagementHubActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000");
        assertTrue(isTextDisplayed("Event not found or not available for organizer management."),
                "Missing-event message expected for an unknown id");
        tapByText("Open My Events");
        assertTrue(isDisplayed(id("recyclerEvents")), "Open My Events should land on the event list");
    }

    @Test
    @DisplayName("EMH-6: Navigating away and back refreshes the hub")
    void awayAndBackRefreshes() {
        tapByText("Edit Event Details");
        assertTrue(isTextDisplayed("Save Changes") || isTextDisplayed("View Event Details"),
                "Edit/View details screen should open");
        pressBack();
        assertTrue(isTextDisplayed("Event Management"), "Hub content should re-render on return");
    }
}