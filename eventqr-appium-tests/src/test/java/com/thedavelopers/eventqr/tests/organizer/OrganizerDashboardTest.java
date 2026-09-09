package com.thedavelopers.eventqr.tests.organizer;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrganizerDashboardTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void loginAsOrganizerAndOpenDashboard() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
    }

    @Test
    @DisplayName("OD-1: Organizer dashboard loads with stats")
    void dashboardLoadsWithStats() {
        assertTrue(dash.isDashboardVisible(), "Organizer dashboard should be visible");
        // TODO: verify stats resource-ids in Appium Inspector
        assertTrue(isDisplayed(id("txtActiveEvents")), "Active events stat should be visible");
    }

    @Test
    @DisplayName("OD-2: Event list loads on dashboard")
    void eventListLoads() {
        assertTrue(dash.isEventListVisible(), "Event list should be visible");
    }

    @Test
    @DisplayName("OD-3: Notification bell is visible")
    void notificationBellVisible() {
        // TODO: verify notification bell resource-id in Appium Inspector
        assertTrue(isDisplayed(id("imgNotification")), "Notification bell should be visible");
    }

    @Test
    @DisplayName("OD-4: Manage events navigates to hub")
    void manageEventsNavigatesToHub() {
        dash.tapManageEvents();
        // TODO: verify event hub resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerEventHub")) || isDisplayed(id("txtMyEvents")),
                "Event management hub should be visible");
    }
}
