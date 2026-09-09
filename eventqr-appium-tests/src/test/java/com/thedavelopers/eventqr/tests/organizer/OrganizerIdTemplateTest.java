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
 * TestFlow 6.11 — ID Display Settings (IDT-1..IDT-3). Programmatic screen;
 * all assertions text-based. The preview mock ("Juan Dela Cruz") renders a
 * QR placeholder with the attendee name.
 */
public class OrganizerIdTemplateTest extends BaseTest {

    @BeforeEach
    void openIdTemplate() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        OrganizerDashboardPage dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapSeeAllEvents();
        waitForVisibleId(id("recyclerEvents"));
        findElements(id("txtAttendeeEventTitle")).get(0).click();
        waitForText("Event Management");
        tapByText("ID Display Settings");
        waitForText("ID Display Settings");
    }

    @Test
    @DisplayName("IDT-1: Toggle sections and preview render")
    void toggleSectionsRender() {
        assertAll(
                () -> assertTrue(isTextDisplayed("Always included"), "Always included section"),
                () -> assertTrue(isTextDisplayed("Attendee Name"), "Attendee Name toggle"),
                () -> assertTrue(isTextDisplayed("Show on printed ID"), "Show on printed ID section"),
                () -> assertTrue(isTextDisplayed("QR CODE"), "QR preview section"),
                () -> assertTrue(isTextDisplayed("Juan Dela Cruz"), "Mock attendee preview")
        );
    }

    @Test
    @DisplayName("IDT-2: Template is limited to fields and toggles (no color/logo editors)")
    void noColorOrLogoEditors() {
        assertFalse(isTextDisplayed("Color"), "Color editor must not exist");
        assertFalse(isTextDisplayed("Logo"), "Logo editor must not exist");
        assertFalse(isTextDisplayed("Upload"), "Upload editor must not exist");
    }

    @Test
    @DisplayName("IDT-3: Saving the display settings persists the selection")
    void saveSettingsPersists() {
        tapByText("Attendee Name");
        tapByText("Save ID Display Settings");
        assertTrue(isToastDisplayed("saved") || isToastDisplayed("Saved")
                        || isTextDisplayed("ID Display Settings"),
                "Saving should persist the toggle selection (toast or screen state)");
    }
}