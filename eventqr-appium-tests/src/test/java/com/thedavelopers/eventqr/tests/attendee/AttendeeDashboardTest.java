package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AttendeeDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttendeeDashboardTest extends BaseTest {

    private AttendeeDashboardPage dash;

    @BeforeEach
    void loginAndOpenDashboard() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        dash = new AttendeeDashboardPage();
    }

    @Test
    @DisplayName("DASH-1: Dashboard loads with welcome header showing user name")
    void welcomeHeaderShowsUserName() {
        // TODO: verify welcome header resource-id in Appium Inspector
        assertTrue(dash.isDashboardVisible(), "Dashboard should be visible");
        assertFalse(dash.getWelcomeText().isBlank(), "Welcome text should contain user name");
    }

    @Test
    @DisplayName("DASH-2: Dashboard shows event stats card")
    void dashboardShowsStatsCard() {
        // TODO: verify stats card resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtEventCount")), "Event count stat should be visible");
    }

    @Test
    @DisplayName("DASH-3: Upcoming events section loads")
    void upcomingEventsSectionLoads() {
        // TODO: verify upcoming events section resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtUpcomingEvents")), "Upcoming events section should be visible");
    }

    @Test
    @DisplayName("DASH-4: Notification bell is visible")
    void notificationBellVisible() {
        assertTrue(isDisplayed(id("imgNotification")), "Notification bell should be visible");
    }

    @Test
    @DisplayName("DASH-5: See-all navigates to event list")
    void seeAllNavigatesToEventList() {
        dash.tapSeeAllEvents();
        // TODO: verify event list screen element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerEvents")), "Event list should be visible");
    }

    @Test
    @DisplayName("DASH-6: Bottom navigation tabs are present")
    void bottomNavTabsPresent() {
        assertAll(
                () -> assertTrue(isDisplayed(id("navEvents")), "Events tab should be visible"),
                () -> assertTrue(isDisplayed(id("navRegistered")), "Registered tab should be visible"),
                () -> assertTrue(isDisplayed(id("navRewards")), "Rewards tab should be visible"),
                () -> assertTrue(isDisplayed(id("navProfile")), "Profile tab should be visible")
        );
    }

    @Test
    @DisplayName("DASH-7: Swipe down refreshes dashboard")
    void swipeDownRefreshes() {
        swipeDown();
        // TODO: verify refresh feedback resource-id in Appium Inspector
        assertTrue(dash.isDashboardVisible(), "Dashboard should remain visible after refresh");
    }
}
