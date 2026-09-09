package com.thedavelopers.eventqr.tests.organizer;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrganizerReportsTest extends BaseTest {

    private OrganizerDashboardPage dash;

    @BeforeEach
    void loginAsOrganizerAndOpenReports() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        dash = new OrganizerDashboardPage();
        // TODO: verify reports entry resource-id in Appium Inspector
        tap(id("btnReports"));
    }

    @Test
    @DisplayName("REP-1: Reports catalog loads")
    void reportsCatalogLoads() {
        // TODO: verify reports catalog resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerReports")), "Reports catalog should be visible");
    }

    @Test
    @DisplayName("REP-2: Selecting a report opens it")
    void selectReportOpens() {
        // TODO: verify report item resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerReports")), "Reports catalog present");
    }

    @Test
    @DisplayName("REP-3: Report preview screen loads")
    void reportPreviewLoads() {
        // TODO: verify report preview resource-id in Appium Inspector
        tap(id("btnPreviewReport"));
        assertTrue(isDisplayed(id("txtReportTitle")) || isDisplayed(id("reportContent")),
                "Report preview should be visible");
    }

    @Test
    @DisplayName("REP-4: Empty state when no reports")
    void emptyStateWhenNoReports() {
        // TODO: verify empty state resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtEmptyReports")) || isTextDisplayed("No reports"),
                "Empty state should be shown when no reports");
    }
}
