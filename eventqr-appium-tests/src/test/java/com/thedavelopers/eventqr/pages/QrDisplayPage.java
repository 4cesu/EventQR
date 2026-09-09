package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * QR Display / QR Credential screen (TestFlow: QR-1..QR-4).
 * Layouts: activity_qr_display.xml, activity_qr_credential.xml (shared element ids)
 */
public class QrDisplayPage extends BaseTest {

    // Top bar (display variant)
    private static final String TOOLBAR_QR_DISPLAY = "toolbarQrDisplay";

    // Credential variant close button
    private static final String CLOSE_BUTTON = "btnCloseQr";

    // QR content
    private static final String QR_IMAGE = "imgQrCode";
    private static final String QR_CREDENTIAL_VALUE = "txtQrCredentialValue";
    private static final String ATTENDEE_NAME = "txtQrAttendeeName";
    private static final String ATTENDEE_EMAIL = "txtQrAttendeeEmail";
    private static final String EVENT_NAME = "txtQrEventName";

    // Download state
    private static final String DOWNLOAD_BUTTON = "btnMarkQrDownloaded";
    private static final String DOWNLOAD_PROGRESS = "progressQrDownload";
    private static final String DOWNLOAD_CHECK = "imgQrDownloadCheck";

    // Manual lookup
    private static final String QR_LOADING = "txtQrLoading";
    private static final String REGISTRATION_ID_FIELD = "edtQrRegistrationId";
    private static final String LOAD_QR_BUTTON = "btnLoadQr";
    private static final String QR_VALUE_LEGACY = "txtQrValue";

    public boolean isQrDisplayVisible() {
        return isDisplayed(id(QR_IMAGE)) || isDisplayed(id(ATTENDEE_NAME));
    }

    public boolean isToolbarDisplayed() {
        return isDisplayed(id(TOOLBAR_QR_DISPLAY));
    }

    public void tapBack() {
        tap(id(TOOLBAR_QR_DISPLAY));
    }

    public boolean isCloseButtonDisplayed() {
        return isDisplayed(id(CLOSE_BUTTON));
    }

    public void tapClose() {
        tap(id(CLOSE_BUTTON));
    }

    public boolean isQrCodeDisplayed() {
        return isDisplayed(id(QR_IMAGE));
    }

    public String getCredentialValue() {
        return getText(id(QR_CREDENTIAL_VALUE));
    }

    public String getAttendeeName() {
        return getText(id(ATTENDEE_NAME));
    }

    public String getAttendeeEmail() {
        return getText(id(ATTENDEE_EMAIL));
    }

    public String getEventName() {
        return getText(id(EVENT_NAME));
    }

    public boolean isDownloadButtonDisplayed() {
        return isDisplayed(id(DOWNLOAD_BUTTON));
    }

    public void tapDownload() {
        tap(id(DOWNLOAD_BUTTON));
    }

    public boolean isDownloadProgressDisplayed() {
        return isDisplayed(id(DOWNLOAD_PROGRESS));
    }

    public boolean isDownloadCheckDisplayed() {
        return isDisplayed(id(DOWNLOAD_CHECK));
    }

    public boolean isLoadingDisplayed() {
        return isDisplayed(id(QR_LOADING));
    }

    /**
     * Manual QR lookup by registration id (only used when the QR must be
     * re-fetched for a given registration).
     */
    public void searchRegistrationId(String registrationId) {
        type(id(REGISTRATION_ID_FIELD), registrationId);
        tap(id(LOAD_QR_BUTTON));
    }
}