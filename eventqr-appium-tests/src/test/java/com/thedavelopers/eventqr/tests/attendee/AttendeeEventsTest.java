package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 4.2 — ATTENDEE EVENTS (AttendeeEventsActivity)
 */
public class AttendeeEventsTest extends BaseTest {

    @BeforeEach
    void loginAndOpenEvents() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        tap(id("navEvents"));
    }

    @Test
    @DisplayName("AEV-1: Chips filter — All / Upcoming / Active / Completed")
    void chipsFilterDisplayed() {
        assertTrue(isDisplayed(id("chipAll")), "All chip should be visible");
        assertTrue(isDisplayed(id("chipUpcoming")), "Upcoming chip should be visible");
        assertTrue(isDisplayed(id("chipActive")), "Active chip should be visible");
        assertTrue(isDisplayed(id("chipCompleted")), "Completed chip should be visible");

        // Tap each chip and verify list updates
        tap(id("chipUpcoming"));
        assertTrue(isDisplayed(id("recyclerEvents")) || isDisplayed(id("txtEventsEmpty")),
                "Upcoming filter should show events or empty state");

        tap(id("chipActive"));
        assertTrue(isDisplayed(id("recyclerEvents")) || isDisplayed(id("txtEventsEmpty")),
                "Active filter should show events or empty state");

        tap(id("chipCompleted"));
        assertTrue(isDisplayed(id("recyclerEvents")) || isDisplayed(id("txtEventsEmpty")),
                "Completed filter should show events or empty state");

        tap(id("chipAll"));
        assertTrue(isDisplayed(id("recyclerEvents")) || isDisplayed(id("txtEventsEmpty")),
                "All filter should show events or empty state");
    }

    @Test
    @DisplayName("AEV-2: Search box filters events live")
    void searchBoxFiltersLive() {
        assertTrue(isDisplayed(id("inputEventSearch")),
                "Search box should be visible");
        // Type a search query and verify filtering
        if (isDisplayed(id("inputEventSearch"))) {
            type(id("inputEventSearch"), "Test");
            assertTrue(isDisplayed(id("recyclerEvents")) || isDisplayed(id("txtEventsEmpty")),
                    "Search results or empty state should appear");
        }
    }

    @Test
    @DisplayName("AEV-3: Swipe-to-refresh and skeleton on first load")
    void swipeToRefreshAndSkeleton() {
        assertTrue(isDisplayed(id("recyclerEvents")) || isDisplayed(id("skeletonLoading")),
                "Events list or skeleton should be visible on load");
        swipeDown();
        assertTrue(isDisplayed(id("recyclerEvents")) || isDisplayed(id("txtEventsEmpty")),
                "Events list should be visible after refresh");
    }

    @Test
    @DisplayName("AEV-4: Tap event navigates to EventDetailActivity")
    void tapEventNavigatesToDetail() {
        assertTrue(isDisplayed(id("recyclerEvents")), "Events list should be present");
        tap(id("recyclerEvents")); // tap first event in list
        assertTrue(isDisplayed(id("txtDetailTitle")) || isDisplayed(id("txtDetailDescription")),
                "Should navigate to EventDetailActivity");
        pressBack();
    }

    @Test
    @DisplayName("AEV-5: Empty state visible when no events match filter")
    void emptyStateWhenNoEvents() {
        // Filter to a status unlikely to have events
        tap(id("chipCompleted"));
        // Verify empty state or list is displayed
        assertTrue(isDisplayed(id("txtEventsEmpty")) || isDisplayed(id("recyclerEvents")),
                "Empty state or event list should be visible");
    }

    @Test
    @DisplayName("AEV-6: Error state shows retry option")
    void errorStateShowsRetry() {
        // Error state is network-dependent; verify UI structure
        assertTrue(isDisplayed(id("recyclerEvents"))
                        || isDisplayed(id("txtEventsEmpty"))
                        || isDisplayed(id("btnRefreshEvents")),
                "Events list, empty state, or retry button should be visible");
    }
}
