package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Staff QR Scanner screen (TestFlow: SC-1..SC-10).
 * Layout: activity_staff_scanner.xml
 */
public class ScannerPage extends BaseTest {

    // Event selector
    private static final String EVENT_CARD = "cardScannerEvent";
    private static final String EVENT_TITLE = "txtScannerEventTitle";
    private static final String EVENT_DATE = "txtScannerEventDate";
    private static final String EVENT_CHEVRON = "txtEventChevron";
    private static final String EVENT_SPINNER = "spnScannerEvent";
    private static final String EMPTY_STATE = "txtScannerEmptyState";

    // Purpose selector
    private static final String PURPOSE_CARD = "cardSelectedPurpose";
    private static final String PURPOSE_NAME = "txtSelectedPurposeName";
    private static final String PURPOSE_POINTS = "txtSelectedPurposePoints";
    private static final String PURPOSE_CHEVRON = "txtPurposeChevron";
    private static final String PURPOSE_SPINNER = "spnScannerPurpose";

    // Camera preview
    private static final String CAMERA_CONTAINER = "frameCameraContainer";
    private static final String CAMERA_PREVIEW = "surfaceInlineCameraPreview";
    private static final String SCANNER_ICON = "imgScannerIcon";
    private static final String CAMERA_STATUS = "txtInlineCameraStatus";

    // Manual entry
    private static final String MANUAL_QR_FIELD = "edtScannerQr";
    private static final String SUBMIT_SCAN_BUTTON = "btnSubmitScan";
    private static final String NOTES_FIELD = "edtScannerNotes";

    // Scan feedback
    private static final String SCAN_RESULT = "txtScannerResult";

    // Bottom nav
    private static final String NAV_DASHBOARD = "navDashboard";
    private static final String NAV_SCANNER = "navScanner";
    private static final String NAV_EVENTS = "navEvents";
    private static final String NAV_LOGS = "navLogs";

    public boolean isScannerVisible() {
        return isDisplayed(id(EVENT_CARD)) && isDisplayed(id(NAV_SCANNER));
    }

    /**
     * Picks an event from the scanner dropdown by visible title.
     */
    public void pickEvent(String eventTitle) {
        tap(id(EVENT_CARD));
        tapByText(eventTitle);
    }

    public String getSelectedEventTitle() {
        return getText(id(EVENT_TITLE));
    }

    public String getSelectedEventDate() {
        return getText(id(EVENT_DATE));
    }

    public boolean isEmptyStateDisplayed() {
        return isDisplayed(id(EMPTY_STATE));
    }

    /**
     * Picks a scan purpose from the dropdown by visible label.
     */
    public void pickPurpose(String purpose) {
        tap(id(PURPOSE_CARD));
        tapByText(purpose);
    }

    public String getSelectedPurposeName() {
        return getText(id(PURPOSE_NAME));
    }

    public boolean isPurposePointsDisplayed() {
        return isDisplayed(id(PURPOSE_POINTS));
    }

    public boolean isCameraPreviewDisplayed() {
        return isDisplayed(id(CAMERA_PREVIEW));
    }

    public boolean isCameraStatusDisplayed() {
        return isDisplayed(id(CAMERA_STATUS));
    }

    public void enterManualQr(String qrValue) {
        type(id(MANUAL_QR_FIELD), qrValue);
    }

    public void tapSubmitScan() {
        tap(id(SUBMIT_SCAN_BUTTON));
    }

    public void enterNotes(String notes) {
        type(id(NOTES_FIELD), notes);
    }

    public boolean isScanResultDisplayed() {
        return isDisplayed(id(SCAN_RESULT));
    }

    public String getScanResult() {
        return getText(id(SCAN_RESULT));
    }

    /**
     * Full manual verification flow.
     */
    public void manualVerify(String qrValue) {
        enterManualQr(qrValue);
        tapSubmitScan();
    }

    /**
     * Scans a QR code value through the inline camera viewfinder area.
     */
    public void scanQrCode() {
        tap(id(CAMERA_CONTAINER));
    }

    public void openDashboardTab() {
        tap(id(NAV_DASHBOARD));
    }

    public void openEventsTab() {
        tap(id(NAV_EVENTS));
    }

    public void openLogsTab() {
        tap(id(NAV_LOGS));
    }
}