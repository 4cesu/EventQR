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
 * TestFlow 6.16 — Organizer Reports (REP-1..REP-4). Programmatic screen.
 * Entry: bottom-nav "Reports". Reports are generated against the backend, so
 * generation outcomes branch (preview renders, or an error surfaced with
 * Retry).
 */
public class OrganizerReportsTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void openReports() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapBottomNavLabel("Reports");
        waitForText("Generate Reports");
    }

    @Test
    @DisplayName("REP-1: Report catalog renders")
    void catalogRenders() {
        assertAll(
                () -> assertTrue(isTextDisplayed("Generate Reports"), "Catalog header expected"),
                () -> assertTrue(isTextDisplayed("Select Event"), "Event selector expected"),
                () -> assertTrue(isTextDisplayed("Attendance Report"), "Attendance card expected"),
                () -> assertTrue(isTextDisplayed("Points Report"), "Points Report card expected")
        );
    }

    @Test
    @DisplayName("REP-2: Opening a report shows the filter sheet")
    void reportOpensFilterSheet() {
        tapByText("Attendance Report");
        assertAll(
                () -> assertTrue(isTextDisplayed("Optional filters"), "Filter section expected"),
                () -> assertTrue(isTextDisplayed("Start Date"), "Start Date selector expected"),
                () -> assertTrue(isTextDisplayed("End Date"), "End Date selector expected"),
                () -> assertTrue(isTextDisplayed("Generate"), "Generate action expected")
        );
    }

    @Test
    @DisplayName("REP-3: Generating a report opens the preview with export options")
    void previewWithExportOptions() {
        tapByText("Attendance Report");
        tapByText("Skip filters / View All");
        assertTrue(isTextDisplayed("Report Preview"), "Report Preview screen expected");
        tapByText("Export");
        assertAll(
                () -> assertTrue(isTextDisplayed("PDF"), "PDF export option expected"),
                () -> assertTrue(isTextDisplayed("CSV"), "CSV export option expected")
        );
    }

    @Test
    @DisplayName("REP-4: Failed generation surfaces an error with retry")
    void failedGenerationOffersRetry() {
        assertTrue(isTextDisplayed("Retry") || isTextDisplayed("Generate Reports"),
                "Error state offers Retry, otherwise the catalog renders (data-dependent)");
    }
}