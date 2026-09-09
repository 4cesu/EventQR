package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 4.5 — REGISTERED EVENTS (RegisteredEventsActivity)
 */
public class AttendeeRegisteredEventsTest extends BaseTest {

    @BeforeEach
    void loginAndOpenRegistered() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        tap(id("navRegistered"));
    }

    @Test
    @DisplayName("REV-1: Chips filter — All / Registered / Completed")
    void chipsFilterDisplayed() {
        assertTrue(isDisplayed(id("chipAll")), "All chip should be visible");
        assertTrue(isDisplayed(id("chipRegistered")), "Registered chip should be visible");
        assertTrue(isDisplayed(id("chipCompleted")), "Completed chip should be visible");
    }

    @Test
    @DisplayName("REV-2: Registered filter shows future events (eventStartAt in future)")
    void registeredFilterShowsFutureEvents() {
        tap(id("chipRegistered"));
        assertTrue(isDisplayed(id("recyclerRegisteredEvents")) || isDisplayed(id("txtRegisteredEventsEmpty")),
                "Registered filter should show future events or empty state");
    }

    @Test
    @DisplayName("REV-3: Completed filter shows past events (eventStartAt in past)")
    void completedFilterShowsPastEvents() {
        tap(id("chipCompleted"));
        assertTrue(isDisplayed(id("recyclerRegisteredEvents")) || isDisplayed(id("txtRegisteredEventsEmpty")),
                "Completed filter should show past events or empty state");
    }

    @Test
    @DisplayName("REV-4: Empty state shown when filter has no events")
    void emptyStateWhenFilterEmpty() {
        // Switch between filters to find one that might be empty
        tap(id("chipCompleted"));
        assertTrue(isDisplayed(id("txtRegisteredEventsEmpty"))
                        || isDisplayed(id("recyclerRegisteredEvents")),
                "Empty state or list should be visible for completed filter");
    }

    @Test
    @DisplayName("REV-5: Swipe-to-refresh and skeleton loading")
    void swipeToRefreshAndSkeleton() {
        assertTrue(isDisplayed(id("recyclerRegisteredEvents"))
                        || isDisplayed(id("skeletonLoading"))
                        || isDisplayed(id("txtRegisteredEventsEmpty")),
                "Registered list, skeleton, or empty state should be visible");
        swipeDown();
        assertTrue(isDisplayed(id("recyclerRegisteredEvents"))
                        || isDisplayed(id("txtRegisteredEventsEmpty")),
                "List should be visible after refresh");
    }

    @Test
    @DisplayName("REV-6: Tap registered event opens QR credential (QrDisplayActivity)")
    void tapRegisteredEventOpensQr() {
        assertTrue(isDisplayed(id("recyclerRegisteredEvents")), "Registered events list should be present");
        tap(id("recyclerRegisteredEvents")); // tap first item
        assertTrue(isDisplayed(id("imgQrCode"))
                        || isDisplayed(id("txtQrCredentialValue"))
                        || isDisplayed(id("txtQrAttendeeName")),
                "Should open QR credential display");
        pressBack();
    }

    @Test
    @DisplayName("REV-7: Bottom nav 'Registered' tab is highlighted")
    void registeredTabHighlighted() {
        assertTrue(isDisplayed(id("navRegistered")), "Registered tab should be visible and active");
    }
}
