package com.thedavelopers.eventqr.tests.organizer;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * TestFlow 6.13 — Transaction Logs (TLOG-1..TLOG-4).
 * TransactionLogsActivity is fully programmatic and reachable only from the
 * organizer attendee details (View Full Log). It requires an attendee record
 * with transactions, so the class assumes a populated backend and skips
 * gracefully when there are no attendees to drill into.
 */
public class OrganizerTransactionLogsTest extends BaseTest {

    private boolean attended = true;

    @BeforeEach
    void openTransactionLogs() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        OrganizerDashboardPage dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapBottomNavLabel("Attendees");
        try {
            waitForVisibleId(id("recyclerAttendees"));
        } catch (Exception e) {
            waitForVisibleId(id("txtAttendeesEmpty"));
        }
        if (findElements(id("txtAttendeeName")).isEmpty()) {
            attended = false;
            return;
        }
        findElements(id("txtAttendeeName")).get(0).click();
        waitForVisibleId(id("txtDetailTitle"));
        tapByText("View Full Log");
        waitForText("Transaction Logs");
    }

    @Test
    @DisplayName("TLOG-1: Transaction logs screen with search renders")
    void logsScreenRenders() {
        assumeTrue(attended, "Requires at least one attendee record in the backend");
        assertTrue(isDisplayedText("Search attendee, QR ID, transaction ID, staff, or event"),
                "Search hint expected");
    }

    @Test
    @DisplayName("TLOG-2: Status filter labels render")
    void statusFiltersRender() {
        assumeTrue(attended, "Requires at least one attendee record in the backend");
        assertAll(
                () -> assertTrue(isTextDisplayed("All"), "All filter expected"),
                () -> assertTrue(isTextDisplayed("Approved"), "Approved filter expected"),
                () -> assertTrue(isTextDisplayed("Rejected"), "Rejected filter expected"),
                () -> assertTrue(isTextDisplayed("Successful"), "Successful filter expected")
        );
    }

    @Test
    @DisplayName("TLOG-3: Searching keeps the log list stable")
    void searchKeepsListStable() {
        assumeTrue(attended, "Requires at least one attendee record in the backend");
        typeIntoHint("Search attendee, QR ID, transaction ID, staff, or event", "zzz");
        assertTrue(isTextDisplayed("All") || isTextDisplayed("No activities found"),
                "Search should keep the screen (with filters or an empty result)");
    }

    @Test
    @DisplayName("TLOG-4: Filtering by status renders without crashing")
    void filterByStatus() {
        assumeTrue(attended, "Requires at least one attendee record in the backend");
        tapByText("Approved");
        assertTrue(isTextDisplayed("All") || isTextDisplayed("No activities found"),
                "Status filter should keep the screen rendered");
    }

    /* ── helpers ──────────────────────────────────────────────────────── */

    private boolean isDisplayedText(String text) {
        return isTextDisplayed(text);
    }

    private void typeIntoHint(String hint, String value) {
        org.openqa.selenium.WebElement el = waitForHint(hint);
        el.clear();
        el.sendKeys(value);
    }

    private org.openqa.selenium.WebElement waitForHint(String hint) {
        return wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(
                io.appium.java_client.AppiumBy.androidUIAutomator(
                        "new UiSelector().text(\"" + hint + "\")")));
    }
}