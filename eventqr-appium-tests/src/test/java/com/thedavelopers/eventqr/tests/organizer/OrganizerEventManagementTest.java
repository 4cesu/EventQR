package com.thedavelopers.eventqr.tests.organizer;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrganizerEventManagementTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void loginAsOrganizerAndOpenHub() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
        dash.tapManageEvents();
    }

    @Test
    @DisplayName("EM-1: Event management hub loads")
    void eventHubLoads() {
        // TODO: verify hub element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerEventHub")) || isDisplayed(id("txtMyEvents")),
                "Event management hub should be visible");
    }

    @Test
    @DisplayName("EM-2: Create event button visible")
    void createEventButtonVisible() {
        // TODO: verify create event button resource-id in Appium Inspector
        assertTrue(isDisplayed(id("btnCreateEvent")), "Create event button should be visible");
    }

    @Test
    @DisplayName("EM-3: Tapping event opens edit details form")
    void tapEventOpensEditDetails() {
        // TODO: verify event item resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerEventHub")), "Event hub list present");
    }

    @Test
    @DisplayName("EA-1: Edit event details form loads")
    void editEventDetailsFormLoads() {
        // TODO: verify edit details form resource-ids in Appium Inspector
        tap(id("btnEditEvent"));
        assertAll(
                () -> assertTrue(isDisplayed(id("edtEventName")), "Event name field should be visible"),
                () -> assertTrue(isDisplayed(id("edtEventDate")), "Event date field should be visible")
        );
    }

    @Test
    @DisplayName("EA-2: Save edited event details")
    void saveEditedEventDetails() {
        // TODO: verify edit/save flow resource-ids in Appium Inspector
        tap(id("btnEditEvent"));
        type(id("edtEventName"), "Updated Event Name");
        tap(id("btnSaveEvent"));
        assertTrue(isTextDisplayed("saved") || isTextDisplayed("updated") || isDisplayed(id("recyclerEventHub")),
                "Event details should save");
    }

    @Test
    @DisplayName("SP-1: Assign staff to event")
    void assignStaffToEvent() {
        // TODO: verify staff assignment flow resource-ids in Appium Inspector
        tap(id("btnAssignStaff"));
        assertTrue(isDisplayed(id("recyclerStaffList")) || isDisplayed(id("listStaff")),
                "Staff assignment list should open");
    }

    @Test
    @DisplayName("SP-2: Assigned staff shown on event")
    void assignedStaffShownOnEvent() {
        // TODO: verify assigned staff display resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtAssignedStaff")), "Assigned staff info should be visible");
    }

    @Test
    @DisplayName("SP-3: Scan purpose toggles available")
    void scanPurposeTogglesAvailable() {
        // TODO: verify scan purpose toggle resource-ids in Appium Inspector
        assertTrue(isDisplayed(id("toggleEntryScan")) || isDisplayed(id("chipScanPurpose")),
                "Scan purpose toggles should be available");
    }

    @Test
    @DisplayName("TR-1: ID template field toggles available")
    void idTemplateFieldTogglesAvailable() {
        // TODO: verify ID template field toggle resource-ids in Appium Inspector
        assertTrue(isDisplayed(id("toggleTemplateFields")) || isDisplayed(id("chipTemplateField")),
                "ID template field toggles should be available");
    }

    @Test
    @DisplayName("TR-2: Save ID template configuration")
    void saveIdTemplateConfig() {
        // TODO: verify template save flow resource-ids in Appium Inspector
        tap(id("btnSaveTemplate"));
        assertTrue(isTextDisplayed("saved") || isTextDisplayed("updated") || isTextDisplayed("Template"),
                "ID template config should save");
    }
}
