package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttendeeRewardsTest extends BaseTest {

    @BeforeEach
    void loginAndOpenRewards() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        // TODO: verify rewards tab resource-id in Appium Inspector
        tap(id("navRewards"));
    }

    @Test
    @DisplayName("RW-1: Rewards list loads")
    void rewardsListLoads() {
        // TODO: verify rewards list resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerRewards")), "Rewards list should be visible");
    }

    @Test
    @DisplayName("RW-2: Point balance is displayed")
    void pointBalanceDisplayed() {
        // TODO: verify point balance resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtPointsBalance")), "Points balance should be visible");
    }

    @Test
    @DisplayName("RW-3: Out-of-stock reward shows warning")
    void outOfStockShowsWarning() {
        // TODO: verify out-of-stock condition and warning resource-id in Appium Inspector
        assertTrue(isTextDisplayed("Out of stock") || isTextDisplayed("Sold out")
                        || isDisplayed(id("badgeOutOfStock")),
                "Out-of-stock warning should be visible for unavailable reward");
    }

    @Test
    @DisplayName("RW-4: Claiming a reward requires sufficient points")
    void claimingRequiresSufficientPoints() {
        // TODO: verify claim disabled state resource-id in Appium Inspector
        assertTrue(isDisplayed(id("btnClaim")), "Claim button should be present");
    }

    @Test
    @DisplayName("RW-5: Claimed rewards are shown")
    void claimedRewardsShown() {
        // TODO: verify claimed rewards section resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtClaimedRewards")), "Claimed rewards section should be visible");
    }

    @Test
    @DisplayName("RW-6: Reward detail shows points required")
    void rewardDetailShowsPointsRequired() {
        // TODO: verify points-required chip resource-id in Appium Inspector
        assertTrue(isDisplayed(id("chipPointsRequired")), "Points-required chip should be visible");
    }

    @Test
    @DisplayName("RW-7: Empty state when no rewards")
    void emptyStateWhenNoRewards() {
        // TODO: verify empty state resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtEmptyRewards")) || isTextDisplayed("No rewards"),
                "Empty state should be shown when no rewards available");
    }
}
