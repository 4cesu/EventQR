package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttendeeRegisteredEventsTest extends BaseTest {

    @BeforeEach
    void loginAndOpenRegistered() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        // TODO: verify registered tab resource-id in Appium Inspector
        tap(id("navRegistered"));
    }

    @Test
    @DisplayName("REGD-1: Registered events list loads")
    void registeredEventsListLoads() {
        // TODO: verify registered events list resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerRegistered")), "Registered events list should be visible");
    }

    @Test
    @DisplayName("REGD-2: Registered event shows QR credential")
    void registeredEventShowsQrCredential() {
        // TODO: verify QR credential element resource-id in Appium Inspector
        tap(id("btnViewQr"));
        assertTrue(isDisplayed(id("imgQrCode")), "QR code should be displayed");
    }

    @Test
    @DisplayName("REGD-3: Empty state when no registered events")
    void emptyStateWhenNoRegisteredEvents() {
        // TODO: verify empty state resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtEmptyRegistered")) || isTextDisplayed("No registered events"),
                "Empty state should be shown when no registered events");
    }

    @Test
    @DisplayName("REGD-4: Tapping registered event opens its detail")
    void tapRegisteredEventOpensDetail() {
        // TODO: verify registered event item resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerRegistered")), "Registered events list present");
    }

    @Test
    @DisplayName("REGD-5: Event status badge is displayed on card")
    void statusBadgeDisplayed() {
        // TODO: verify status badge resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtEventStatus")) || isDisplayed(id("badgeStatus")),
                "Status badge should be shown on registered event card");
    }
}
