package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttendeeEventsTest extends BaseTest {

    @BeforeEach
    void loginAndOpenEvents() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        // TODO: verify events tab resource-id in Appium Inspector
        tap(id("navEvents"));
    }

    @Test
    @DisplayName("EVT-1: Events list loads available events")
    void eventsListLoads() {
        // TODO: verify events list resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerEvents")), "Events list should be visible");
    }

    @Test
    @DisplayName("EVT-2: Filter chips are displayed")
    void filterChipsDisplayed() {
        // TODO: verify filter chips resource-ids in Appium Inspector
        assertAll(
                () -> assertTrue(isDisplayed(id("chipAll")), "All chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipUpcoming")), "Upcoming chip should be visible")
        );
    }

    @Test
    @DisplayName("EVT-3: Filtering by status updates list")
    void filterByStatusUpdatesList() {
        // TODO: verify filter chip resource-id in Appium Inspector
        tap(id("chipUpcoming"));
        // TODO: verify filtered list state resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerEvents")), "Filtered list should still be visible");
    }

    @Test
    @DisplayName("EVT-4: Tapping event opens detail")
    void tapEventOpensDetail() {
        // TODO: verify event item resource-id in Appium Inspector
        tapByTextContains("Event"); // fallback
        // TODO: verify detail screen element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtEventTitle")), "Event detail should be visible");
    }

    private void tapByTextContains(String text) {
        waitForTextContains(text).click();
    }

    @Test
    @DisplayName("EVT-5: Register button on detail registers attendee")
    void registerButtonWorks() {
        // TODO: verify event detail register button resource-id in Appium Inspector
        tap(id("btnRegisterEvent"));
        assertTrue(isTextDisplayed("Registered")
                        || isTextDisplayed("Success")
                        || isTextDisplayed("registered"),
                "Registration confirmation should appear");
    }

    @Test
    @DisplayName("EVT-6: Empty state visible when no events match")
    void emptyStateVisible() {
        // TODO: verify empty state resource-id and trigger condition in Appium Inspector
        assertTrue(isDisplayed(id("txtEmptyEvents")) || isTextDisplayed("No events"),
                "Empty state should be visible when list is empty");
    }

    @Test
    @DisplayName("EVT-7: Event card shows capacity info")
    void eventCardShowsCapacity() {
        // TODO: verify capacity text resource-id in Appium Inspector
        assertTrue(isTextDisplayed("/") || isDisplayed(id("txtCapacity")),
                "Capacity information should be shown on event card");
    }
}
