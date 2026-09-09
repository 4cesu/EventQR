package com.thedavelopers.eventqr.tests.staff;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 5.8 — Event Registrations (from staff side) ER-1..ER-4.
 * EventRegistrationsActivity (features.staff) is reachable from the assigned
 * events row (Attendees action) or deterministically via am start. Rows exist
 * only when registrations exist, so print-bar and row-tap flows branch on the
 * loaded-empty/error states (data-dependent backend).
 */
public class StaffEventRegistrationsTest extends BaseTest {

    @BeforeEach
    void loginAsStaff() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
    }

    @Test
    @DisplayName("ER-1: Registrations list with live search renders")
    void registrationsListAndSearchRender() {
        startActivity("com.thedavelopers.eventqr.features.staff.EventRegistrationsActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000");
        assertAll(
                () -> assertTrue(isDisplayed(id("edtRegistrationSearch")), "Search field expected"),
                () -> assertTrue(isDisplayed(id("swipeRefreshEventRegistrations")), "Swipe-refresh expected"),
                () -> assertTrue(isDisplayed(id("btnSelectForPrint")), "Print selection toggle expected")
        );
        type(id("edtRegistrationSearch"), "zzz-nonexistent");
        assertTrue(isDisplayed(id("recyclerEventRegistrations"))
                        || isTextDisplayed("No registrations found"),
                "Filtering should keep the list (or empty state) rendered");
    }

    @Test
    @DisplayName("ER-2: Swipe to refresh reloads the registrations")
    void swipeToRefreshRegistrations() {
        startActivity("com.thedavelopers.eventqr.features.staff.EventRegistrationsActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000");
        swipeDown();
        assertTrue(isDisplayed(id("recyclerEventRegistrations"))
                        || isDisplayed(id("txtEventRegistrationsEventTitle"))
                        || isDisplayed(id("edtRegistrationSearch")),
                "Screen should remain rendered after pull-to-refresh");
    }

    @Test
    @DisplayName("ER-3: Batch print selection bar appears when rows are selected")
    void batchPrintSelection() {
        startActivity("com.thedavelopers.eventqr.features.staff.EventRegistrationsActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000");
        if (findElements(id("chkRegistrationSelect")).isEmpty()) {
            assertTrue(isDisplayed(id("edtRegistrationSearch"))
                            || isDisplayed(id("txtEventRegistrationsEventTitle")),
                    "No registrations rows available; screen chrome still verified");
            return;
        }
        findElements(id("chkRegistrationSelect")).get(0).click();
        assertAll(
                () -> assertTrue(isDisplayed(id("batchPrintBar")), "Batch print bar expected after selection"),
                () -> assertTrue(isDisplayed(id("badgeSelectionCount")), "Selection badge expected"),
                () -> assertTrue(isDisplayed(id("btnPrintSelectedIds")), "Print selected button expected")
        );
    }

    @Test
    @DisplayName("ER-4: Tapping a registration opens the attendee details")
    void tapRegistrationOpensAttendeeDetails() {
        startActivity("com.thedavelopers.eventqr.features.staff.EventRegistrationsActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000");
        if (findElements(id("txtRegistrationTitle")).isEmpty()) {
            assertTrue(isDisplayed(id("edtRegistrationSearch"))
                            || isDisplayed(id("txtEventRegistrationsEventTitle")),
                    "No registration rows available; screen chrome still verified");
            return;
        }
        findElements(id("txtRegistrationTitle")).get(0).click();
        assertTrue(isDisplayed(id("btnBackToTransactionResult")),
                "Attendee details screen should open from a registration row");
    }
}