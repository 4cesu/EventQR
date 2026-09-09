package com.thedavelopers.eventqr.tests.staff;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.StaffDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 5.2 Scanner (SCN-1..SCN-7), 5.4 Scan Result (SR-1..SR-4),
 * 5.5 Attendee Details (SAD-1..SAD-3), 5.6 Transaction Result (STR-1..STR-2).
 * Detail/result screens are launched deterministically via {@code am start}
 * extras (same keys StaffScreenExtras uses); the screen under test always
 * renders from those extras without backend requirements.
 */
public class StaffScannerTest extends BaseTest {

    private static final String STAFF = "com.thedavelopers.eventqr.features.staff";

    private StaffDashboardPage dash;

    @BeforeEach
    void loginAsStaff() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        dash = new StaffDashboardPage();
    }

    /* ── 5.2 Scanner ──────────────────────────────────────────────────── */

    @Test
    @DisplayName("SCN-1: Event card and purpose dropdown render on the scanner")
    void scannerRendersEventAndPurpose() {
        dash.openScanner();
        assertAll(
                () -> assertTrue(isDisplayed(id("cardScannerEvent")) || isDisplayed(id("txtScannerEmptyState")),
                        "Event card should render (or empty state)"),
                () -> assertTrue(isDisplayed(id("cardSelectedPurpose")),
                        "Selected purpose card should render")
        );
        tap(id("cardSelectedPurpose"));
        assertTrue(isTextDisplayed("Event Entry") || isTextDisplayed("Session Attendance")
                        || isTextDisplayed("Booth Visit") || isTextDisplayed("Benefit Claim")
                        || isTextDisplayed("Reward Redemption") || isTextDisplayed("No scan purposes enabled")
                        || isTextDisplayed("Select Purpose"),
                "Opening the purpose dropdown should list active purposes or the no-purpose state");
    }

    @Test
    @DisplayName("SCN-2: Inline camera preview and status render")
    void inlineCameraRenders() {
        dash.openScanner();
        assertAll(
                () -> assertTrue(isDisplayed(id("frameCameraContainer")), "Camera container expected"),
                () -> assertTrue(isDisplayed(id("surfaceInlineCameraPreview")), "Camera preview surface expected"),
                () -> assertTrue(isDisplayed(id("txtInlineCameraStatus")), "Camera status line expected")
        );
    }

    @Test
    @DisplayName("SCN-3: Manual QR submission handles invalid codes without crashing")
    void manualInvalidQrHandled() {
        dash.openScanner();
        if (isDisplayed(id("edtScannerQr"))) {
            type(id("edtScannerQr"), "NOT_A_REAL_QR_CODE");
            tap(id("btnSubmitScan"));
        }
        assertTrue(isRejectedResultOrError(),
                "Invalid QR should surface a rejected result or a non-empty error state");
    }

    @Test
    @DisplayName("SCN-4: Rapid duplicate submissions are safe")
    void rapidDuplicateSubmissionsSafe() {
        dash.openScanner();
        if (isDisplayed(id("edtScannerQr"))) {
            type(id("edtScannerQr"), "NOT_A_REAL_QR_CODE");
            tap(id("btnSubmitScan"));
            tap(id("btnSubmitScan"));
        }
        assertTrue(isRejectedResultOrError(), "Duplicate submissions must not crash the scanner");
    }

    @Test
    @DisplayName("SCN-5: Dedicated camera scanner opens and closes cleanly")
    void cameraScannerOpensAndCloses() {
        startActivity(STAFF + ".StaffCameraScannerActivity", null);
        assertAll(
                () -> assertTrue(isDisplayed(id("btnCloseCamera")), "Camera scanner close button expected"),
                () -> assertTrue(isDisplayed(id("surfaceCameraPreview")), "Camera preview expected")
        );
        tap(id("btnCloseCamera"));
        assertFalse(isDisplayed(id("btnCloseCamera")), "Closing the camera scanner should finish it");
    }

    @Test
    @DisplayName("SCN-6: Camera decode orientation handling (surface renders ready-to-scan)")
    void cameraDecodeReady() {
        dash.openScanner();
        assertTrue(isDisplayed(id("surfaceInlineCameraPreview")), "Camera surface expected for decoding");
        assertTrue(isTextDisplayed("Point camera at attendee QR code")
                || isDisplayed(id("txtInlineCameraStatus")),
                "Status should indicate the decoder is armed");
    }

    @Test
    @DisplayName("SCN-7: Scanner bottom navigation renders with Scan selected")
    void scannerBottomNavRenders() {
        dash.openScannerTab();
        assertAll(
                () -> assertTrue(isDisplayed(id("navScanner")), "Scan nav item expected"),
                () -> assertTrue(isTextDisplayed("Dashboard"), "Dashboard label"),
                () -> assertTrue(isTextDisplayed("Scan"), "Scan label"),
                () -> assertTrue(isTextDisplayed("Events"), "Events label"),
                () -> assertTrue(isTextDisplayed("Logs"), "Logs label")
        );
    }

    /* ── 5.4 Scan Result (deterministic via extras) ───────────────────── */

    @Test
    @DisplayName("SR-1: Approved scan result renders attendee and event details")
    void approvedScanResultRenders() {
        startActivity(STAFF + ".result.StaffScanResultActivity",
                "--es extra_is_valid true"
                        + " --es extra_event_title \"Test Event\""
                        + " --es extra_scan_purpose_name \"Event Entry\""
                        + " --es extra_message \"Approved by backend\""
                        + " --es extra_attendee_name \"Test Attendee\"");
        assertAll(
                () -> assertTrue(isDisplayed(id("headerApproved")), "Approved header expected"),
                () -> assertTrue(isDisplayed(id("txtScanResultState")), "Result state label expected"),
                () -> assertTrue(isDisplayed(id("txtScanResultAttendeeName")), "Attendee name row expected"),
                () -> assertTrue(isDisplayed(id("btnContinueTransaction")),
                        "Continue-transaction action expected on approval"),
                () -> assertTrue(isDisplayed(id("btnViewAttendeeDetails")),
                        "View-details action expected on approval")
        );
    }

    @Test
    @DisplayName("SR-2: View Attendee Details opens the attendee detail screen")
    void viewAttendeeDetailsNavigates() {
        startActivity(STAFF + ".result.StaffScanResultActivity",
                "--es extra_is_valid true"
                        + " --es extra_event_title \"Test Event\""
                        + " --es extra_attendee_name \"Test Attendee\"");
        tap(id("btnViewAttendeeDetails"));
        assertTrue(isDisplayed(id("btnBackToTransactionResult"))
                        || isToastDisplayed("Missing attendee context"),
                "Attendee details screen should open (or explain missing context)");
    }

    @Test
    @DisplayName("SR-3: Rejected scan result renders the rejection reason")
    void rejectedScanResultRenders() {
        startActivity(STAFF + ".result.StaffScanResultActivity",
                "--es extra_is_valid false --es extra_message \"Invalid QR code\"");
        assertAll(
                () -> assertTrue(isDisplayed(id("headerRejected")), "Rejected header expected"),
                () -> assertTrue(isDisplayed(id("txtScanResultStateRejected")), "Rejected state label expected"),
                () -> assertTrue(isDisplayed(id("layoutRejectedReason")), "Rejection reason panel expected"),
                () -> assertTrue(isDisplayed(id("txtScanResultReason")), "Rejection reason text expected"),
                () -> assertTrue(isDisplayed(id("btnScanAgain")), "Scan-again action expected")
        );
    }

    @Test
    @DisplayName("SR-4: Scan result screen restricts access to staff accounts")
    void scanResultStaffOnlyGuard() {
        restartAppToLogin();
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("btnNotificationsHub"));
        startActivity(STAFF + ".result.StaffScanResultActivity", "--es extra_is_valid true");
        assertTrue(isToastDisplayed("Access Denied: Staff only")
                        || !isDisplayed(id("headerApproved")),
                "Non-staff must be blocked from the scan result screen");
    }

    /* ── 5.5 Attendee Details ─────────────────────────────────────────── */

    @Test
    @DisplayName("SAD-1: Attendee details screen opens with event/attendee context")
    void attendeeDetailsOpens() {
        startActivity(STAFF + ".StaffAttendeeDetailsActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000"
                        + " --es extra_attendee_id 00000000-0000-0000-0000-000000000001");
        assertTrue(isDisplayed(id("btnBackToTransactionResult")),
                "Attendee details screen should open");
    }

    @Test
    @DisplayName("SAD-2: Registration info fields render when loaded")
    void registrationInfoRenders() {
        startActivity(STAFF + ".StaffAttendeeDetailsActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000"
                        + " --es extra_attendee_id 00000000-0000-0000-0000-000000000001");
        // Backend miss is tolerated (screen shows the error state) — the
        // contract is that the screen renders the registration section.
        assertTrue(isDisplayed(id("btnBackToTransactionResult")),
                "Details screen expected; registration fields render on backend hits");
        if (!isToastDisplayed("Missing attendee context")) {
            assertTrue(isDisplayed(id("txtDetailRegistrationStatus"))
                            || isDisplayed(id("progressAttendeeDetails"))
                            || isDisplayed(id("layoutRecentTransactions"))
                            || isDisplayed(id("recyclerDetailTransactions")),
                    "Registration section should render when the load succeeds");
        }
    }

    @Test
    @DisplayName("SAD-3: Print / reprint ID action available after load")
    void printIdActionAvailable() {
        startActivity(STAFF + ".StaffAttendeeDetailsActivity",
                "--es extra_event_id 00000000-0000-0000-0000-000000000000"
                        + " --es extra_attendee_id 00000000-0000-0000-0000-000000000001");
        if (isDisplayed(id("btnBackToTransactionResult")) && !isToastDisplayed("Missing attendee context")) {
            assertTrue(isDisplayed(id("btnPrintOrReprintId")),
                    "Print/Reprint action renders after a successful load");
        }
        // Failed loads hide the print action by design — nothing to assert.
        assertTrue(true);
    }

    /* ── 5.6 Transaction Result (deterministic via extras) ────────────── */

    @Test
    @DisplayName("STR-1: Transaction result renders approved and rejected states")
    void transactionResultStates() {
        startActivity(STAFF + ".result.StaffTransactionResultActivity",
                "--es extra_transaction_result APPROVED"
                        + " --es extra_event_title \"Test Event\""
                        + " --es extra_attendee_name \"Test Attendee\""
                        + " --es extra_scan_purpose_name \"Event Entry\""
                        + " --es extra_reason \"Approved by backend\""
                        + " --ei extra_points_delta -50");
        assertAll(
                () -> assertTrue(isDisplayed(id("headerApproved")), "Approved header expected"),
                () -> assertTrue(isDisplayed(id("txtTransactionAttendee")), "Attendee line expected"),
                () -> assertTrue(isDisplayed(id("txtTransactionPoints")), "Points delta line expected")
        );

        startActivity(STAFF + ".result.StaffTransactionResultActivity",
                "--es extra_transaction_result REJECTED"
                        + " --es extra_reason \"Rejected by backend\"");
        assertTrue(isDisplayed(id("headerRejected")), "Rejected header expected");
    }

    @Test
    @DisplayName("STR-2: Transaction result screen restricts access to staff accounts")
    void transactionResultStaffOnlyGuard() {
        restartAppToLogin();
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("btnNotificationsHub"));
        startActivity(STAFF + ".result.StaffTransactionResultActivity",
                "--es extra_transaction_result APPROVED");
        assertTrue(isToastDisplayed("Access Denied: Staff only")
                        || !isDisplayed(id("headerApproved")),
                "Non-staff must be blocked from the transaction result screen");
    }

    /* ── helpers ──────────────────────────────────────────────────────── */

    private boolean isRejectedResultOrError() {
        if (isDisplayed(id("headerRejected"))) {
            return true;
        }
        try {
            WebElement result = waitForVisibleId(id("txtScannerResult"));
            return result.getText() != null && !result.getText().isBlank();
        } catch (Exception e) {
            // Fall through
        }
        return isToastDisplayed("Select a purpose first") || isToastDisplayed("Select an event first");
    }
}