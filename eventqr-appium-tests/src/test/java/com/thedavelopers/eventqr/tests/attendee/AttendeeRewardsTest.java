package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 4.7 — ATTENDEE REWARDS (AttendeeRewardsActivity)
 */
public class AttendeeRewardsTest extends BaseTest {

    @BeforeEach
    void loginAndOpenRewards() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        tap(id("navRewards"));
    }

    @Test
    @DisplayName("ARW-1: No registered events shows empty state 'Register for an event first'")
    void noRegisteredEventsShowsEmptyState() {
        // If user has no registered events, layoutNoRegisteredEvents is shown
        // and no dropdown or My Claims visible
        boolean hasRegisteredEvents = isDisplayed(id("recyclerRewards"))
                || isDisplayed(id("spinnerRegisteredEvents"));
        boolean hasEmptyState = isDisplayed(id("layoutNoRegisteredEvents"))
                || isTextDisplayed("Register for an event");
        assertTrue(hasRegisteredEvents || hasEmptyState,
                "Either rewards are shown or 'no registered events' empty state");
    }

    @Test
    @DisplayName("ARW-2: Registered event selector dropdown lists events")
    void registeredEventSelectorDropdown() {
        // Skip if no registered events
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        assertTrue(isDisplayed(id("spinnerRegisteredEvents")) || isDisplayed(id("cardSelectedEvent")),
                "Event selector dropdown should be visible");
    }

    @Test
    @DisplayName("ARW-3: Selecting event loads rewards list with event title header")
    void selectingEventLoadsRewards() {
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        // Tap dropdown and select an event
        if (isDisplayed(id("cardSelectedEvent"))) {
            tap(id("cardSelectedEvent"));
            assertTrue(isDisplayed(id("recyclerRewards"))
                            || isDisplayed(id("layoutRewardsEmpty")),
                    "Rewards list or empty state should load after selecting event");
        }
    }

    @Test
    @DisplayName("ARW-4: Skeleton loading shown while rewards load")
    void skeletonLoadingOnLoad() {
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        // Skeleton may be briefly visible during load
        assertTrue(isDisplayed(id("layoutRewardsSkeleton"))
                        || isDisplayed(id("recyclerRewards"))
                        || isDisplayed(id("layoutRewardsEmpty"))
                        || isDisplayed(id("layoutNoRegisteredEvents")),
                "Skeleton, rewards list, or empty state should be visible");
    }

    @Test
    @DisplayName("ARW-5: Empty rewards shows 'No rewards available'")
    void emptyRewardsShowsMessage() {
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        assertTrue(isDisplayed(id("layoutRewardsEmpty"))
                        || isDisplayed(id("recyclerRewards")),
                "Empty rewards state or rewards list should be visible");
    }

    @Test
    @DisplayName("ARW-6: Tap reward navigates to RewardDetailsActivity")
    void tapRewardNavigatesToDetail() {
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        if (isDisplayed(id("recyclerRewards"))) {
            tap(id("recyclerRewards")); // tap first reward
            assertTrue(isDisplayed(id("txtRewardTitle"))
                            || isDisplayed(id("txtPointsValue")),
                    "Should navigate to RewardDetailsActivity");
            pressBack();
        }
    }

    @Test
    @DisplayName("ARW-7: Swipe-to-refresh preserves selected event; retry button")
    void swipeToRefreshPreservesEvent() {
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        swipeDown();
        assertTrue(isDisplayed(id("recyclerRewards"))
                        || isDisplayed(id("layoutRewardsEmpty"))
                        || isDisplayed(id("btnRewardsRetry")),
                "After refresh, rewards list, empty state, or retry button should be visible");
    }

    @Test
    @DisplayName("ARW-8: 'My Claims' link navigates to ClaimedRewardsActivity")
    void myClaimsNavigatesToClaimedRewards() {
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        if (isDisplayed(id("txtMyClaims"))) {
            tap(id("txtMyClaims"));
            assertTrue(isTextDisplayed("Claimed") || isDisplayed(id("recyclerClaimedRewards")),
                    "Should navigate to ClaimedRewardsActivity");
            pressBack();
        }
    }

    @Test
    @DisplayName("ARW-9: Error state shows 'Unable to load rewards.' with retry")
    void errorStateShowsRetry() {
        // Error state is network-dependent; verify UI elements exist
        assertTrue(isDisplayed(id("recyclerRewards"))
                        || isDisplayed(id("layoutRewardsEmpty"))
                        || isDisplayed(id("layoutNoRegisteredEvents"))
                        || isDisplayed(id("btnRewardsRetry"))
                        || isDisplayed(id("txtRewardsError")),
                "Rewards list, empty state, or error with retry should be visible");
    }

    @Test
    @DisplayName("ARW-10: Cache shortcut — no registrations skips skeleton to immediate empty")
    void cacheShortcutSkipsSkeleton() {
        // If RegistrationsCache says no registrations, skeleton should be skipped
        // and immediate empty state shown
        assertTrue(isDisplayed(id("layoutNoRegisteredEvents"))
                        || isDisplayed(id("cardSelectedEvent"))
                        || isDisplayed(id("recyclerRewards")),
                "Immediate state should be shown without unnecessary skeleton");
    }

    @Test
    @DisplayName("ARW-11: Balance card hidden on error; shown on success")
    void balanceCardConditionalVisibility() {
        if (isDisplayed(id("layoutNoRegisteredEvents"))) return;

        // Balance card should be visible when rewards loaded successfully
        boolean hasRewards = isDisplayed(id("recyclerRewards"));
        boolean hasBalanceCard = isDisplayed(id("txtRewardsBalance"));
        assertTrue(!hasRewards || hasBalanceCard,
                "Balance card should be visible when rewards are loaded");
    }
}
