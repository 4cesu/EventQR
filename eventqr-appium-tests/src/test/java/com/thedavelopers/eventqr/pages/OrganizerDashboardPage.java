package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page-object for the organizer/user dashboard plus the Request Event form
 * ({@code activity_request_event.xml}).
 *
 * <p>An ORGANIZER logs in to {@code OrganizerDashboardActivity} (organizer
 * portal), which has no request-submission UI. The "Request Event" flow only
 * lives on the attendee/user dashboard ({@code activity_user_dashboard.xml}),
 * reachable via the portal switcher's "Attendee Portal" option. This page
 * object therefore navigates from the organizer portal into the attendee
 * portal before seeding, and operates on the user dashboard afterwards.</p>
 *
 * <p>Provides navigation helpers and a seed flow that creates a pending event
 * request entirely through the UI, so tests that need admin-visible requests
 * are self-contained.</p>
 */
public class OrganizerDashboardPage extends BaseTest {

    /* ── Resource IDs (verified against EventQRMobile source) ──────────── */

    // Organizer dashboard (activity_organizer_dashboard.xml)
    private static final String PORTAL_SWITCHER_CHIP = "portalSwitcherChip";
    private static final String TXT_HEADER_TITLE     = "txtHeaderTitle";
    private static final String TXT_HEADER_SUBTITLE  = "txtHeaderSubtitle";
    private static final String STAT_TOTAL_EVENTS    = "txtStatTotalEvents";
    private static final String STAT_TOTAL_ATTENDEES = "txtStatTotalAttendees";
    private static final String STAT_TRANSACTIONS    = "txtStatTransactions";
    private static final String STAT_REWARDS_GIVEN   = "txtStatRewardsGiven";
    private static final String ACTIVE_EVENTS_CONTAINER = "activeEventsContainer";
    private static final String BTN_SEE_ALL_EVENTS   = "btnSeeAllEvents";
    private static final String LAYOUT_EVENTS_EMPTY  = "layoutEventsEmpty";
    private static final String LAYOUT_DASHBOARD_ERROR = "layoutDashboardError";
    private static final String TXT_DASHBOARD_ERROR  = "txtDashboardError";
    private static final String BTN_DASHBOARD_RETRY  = "btnDashboardRetry";
    private static final String SWIPE_REFRESH_DASHBOARD = "swipeRefreshDashboard";

    // Active-event cards reuse the attendee event-card layout ids
    private static final String CARD_EVENT_TITLE    = "txtAttendeeEventTitle";

    // Bottom nav rows are text-labelled (OrganizerScreenHelpers nav labels)
    public static final String NAV_ATTENDEES_LABEL  = "Attendees";
    public static final String NAV_LOGS_LABEL       = "Logs";
    public static final String NAV_REPORTS_LABEL    = "Reports";
    public static final String NAV_REWARDS_LABEL    = "Rewards";

    // User dashboard (activity_user_dashboard.xml)
    private static final String REQUEST_EVENT_BTN = "btnNotificationsHub";
    private static final String NAV_PROFILE       = "navProfile";
    private static final String TXT_WELCOME       = "txtDashboardWelcome";

    // Request Event form (activity_request_event.xml)
    private static final String EVENT_NAME_INPUT          = "eventNameInput";
    private static final String CARD_EVENT_CATEGORY       = "cardEventCategory";
    private static final String EVENT_DESCRIPTION_INPUT   = "eventDescriptionInput";
    private static final String TARGET_AUDIENCE_INPUT     = "targetAudienceInput";
    private static final String CAPACITY_INPUT            = "capacityInput";
    private static final String VENUE_INPUT               = "venueInput";
    private static final String START_DATE_TIME_INPUT     = "startDateTimeInput";
    private static final String END_DATE_TIME_INPUT       = "endDateTimeInput";
    private static final String REG_START_DATE_TIME_INPUT = "registrationStartDateTimeInput";
    private static final String REG_END_DATE_TIME_INPUT   = "registrationEndDateTimeInput";
    private static final String REQUESTER_NAME_INPUT      = "requesterNameInput";
    private static final String CONTACT_EMAIL_INPUT       = "contactEmailInput";
    private static final String CONTACT_NUMBER_INPUT      = "contactNumberInput";
    private static final String REASON_FOR_REQUEST_INPUT  = "reasonForRequestInput";
    private static final String SUBMIT_REQUEST_BUTTON     = "submitRequestButton";

    // Success dialog (dialog_event_request_submitted.xml)
    private static final String SUCCESS_DASHBOARD_BUTTON = "successDashboardButton";

    // Profile sign-out (activity_profile.xml)
    private static final String BTN_PROFILE_LOGOUT = "btnProfileLogout";

    // Native Android picker dialog ids (framework resources — verified live
    // on Medium_Phone_API_36: AOSP DatePickerDialog then TimePickerDialog)
    private static final String ID_DATE_PICKER_HEADER = "android:id/date_picker_header_date";
    private static final String ID_NEXT_MONTH         = "android:id/next";
    private static final String ID_TIME_HEADER        = "android:id/time_header";
    private static final String ID_INPUT_MODE_BUTTON  = "android:id/toggle_mode";
    private static final String ID_INPUT_HOUR         = "android:id/input_hour";
    private static final String ID_INPUT_MINUTE       = "android:id/input_minute";
    private static final String ID_AM_PM_SPINNER      = "android:id/am_pm_spinner";
    private static final String ID_DIALOG_OK          = "android:id/button1";

    /* ── Dashboard helpers ─────────────────────────────────────────────── */

    public boolean isDashboardVisible() {
        return isDisplayed(id(TXT_WELCOME));
    }

    public void tapRequestEvent() {
        tap(id(REQUEST_EVENT_BTN));
    }

    public void tapProfile() {
        tap(id(NAV_PROFILE));
    }

    public void tapManageEvents() {
        tap(id(BTN_SEE_ALL_EVENTS));
    }

    public void tapNotificationBell() {
        tap(id("imgNotification"));
    }

    public boolean isEventListVisible() {
        return isDisplayed(id("recyclerEvents"));
    }

    /* ── Organizer dashboard (organizer portal) ────────────────────────── */

    /** True while the organizer dashboard header ("Organizer Portal" + name) is up. */
    public boolean isOrganizerDashboardVisible() {
        return isDisplayed(id(TXT_HEADER_TITLE));
    }

    public String getHeaderSubtitle() {
        return getText(id(TXT_HEADER_SUBTITLE));
    }

    public boolean isDisplayedHeaderSubtitle() {
        return isDisplayed(id(TXT_HEADER_SUBTITLE));
    }

    public boolean areStatsVisible() {
        return isDisplayed(id(STAT_TOTAL_EVENTS)) && isDisplayed(id(STAT_TOTAL_ATTENDEES))
                && isDisplayed(id(STAT_TRANSACTIONS)) && isDisplayed(id(STAT_REWARDS_GIVEN));
    }

    /** True when the "active events" section rendered at least one card. */
    public boolean hasActiveEventCards() {
        return !findElements(id(CARD_EVENT_TITLE)).isEmpty();
    }

    public boolean showsEventsEmpty() {
        return isDisplayed(id(LAYOUT_EVENTS_EMPTY));
    }

    public boolean showsDashboardError() {
        return isDisplayed(id(LAYOUT_DASHBOARD_ERROR));
    }

    /** Opens the first active-event card (taps its title; bubbles to row click). */
    public void openFirstActiveEvent() {
        waitForVisibleId(id(CARD_EVENT_TITLE)).click();
    }

    public void tapSeeAllEvents() {
        tap(id(BTN_SEE_ALL_EVENTS));
    }

    public void tapPortalSwitcher() {
        tap(id(PORTAL_SWITCHER_CHIP));
    }

/** Taps an organizer bottom-nav row by its text label. */
    public void tapBottomNavLabel(String label) {
        tapByText(label);
    }

    /**
     * Opens the organizer notification management screen via the dynamic bell
     * (a FrameLayout exposed with contentDescription "Notifications").
     */
    public void tapOrganizerNotificationBell() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().description(\"Notifications\")"))).click();
    }

    /**
     * Seeds an APPROVED event owned by the current organizer if the dashboard
     * has none. Fast path: when the backend already serves cards, no-op.
     * Slow path (fresh databases): organizer → Attendee Portal → Request Event
     * → admin approves via Requests → admin switches to Attendee Portal and
     * signs out → organizer signs back in and lands on the organizer dashboard.
     *
     * <p>Must be invoked right after organizer login, while the organizer
     * dashboard is visible. Waits for the skeleton to settle before deciding
     * whether seeding is needed.</p>
     */
    public void seedApprovedEventIfNone() {
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            if (hasActiveEventCards()) {
                return; // backend already serves owned approved events
            }
            if (isVisibleQuick(id(LAYOUT_EVENTS_EMPTY))) {
                break; // dashboard rendered the empty state → seed
            }
            pause(500);
        }
        if (hasActiveEventCards()) {
            return;
        }

        seedEventRequest(uniqueEventName());

        // Approve it as admin.
        signOut();
        LoginPage login = new LoginPage();
        login.login(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        AdminDashboardPage admin = new AdminDashboardPage();
        admin.openRequestsTab();
        admin.openFirstPendingRequestDetail();
        tap(id("buttonApprove"));
        tap(id("buttonConfirmAction"));
        waitForVisibleId(id("buttonDone"));
        tap(id("buttonDone"));
        pressBack();

        // Admin dashboard has no sign-out — leave via the Attendee Portal.
        admin.tapPortalSwitcher();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().text(\"Attendee Portal\")"))).click();
        waitForVisibleId(id(REQUEST_EVENT_BTN));
        signOut();

        // Re-login as organizer: lands on organizer dashboard with an approved event.
        LoginPage orgLogin = new LoginPage();
        orgLogin.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        waitForVisibleId(id(TXT_HEADER_TITLE));
    }

    private String uniqueEventName() {
        return "Auto Approved Event " + System.currentTimeMillis();
    }

    private boolean isVisibleQuick(String resourceId) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            AppiumBy.id(resourceId)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * From the organizer portal, opens the portal switcher and selects the
     * Attendee Portal so the driver lands on the user dashboard
     * ({@code DashboardActivity}) — the only dashboard that offers the
     * "Request Event" form. Safely no-ops when already on the user dashboard.
     */
    public void switchToAttendeePortal() {
        // On the organizer dashboard the bottom-nav has no Profile tab, so
        // if we are not there yet (e.g. already on the user dashboard) skip.
        // Detect the organizer dashboard by its portal-switcher chip.
        try {
            if (!isDisplayed(id(PORTAL_SWITCHER_CHIP))) {
                return; // already on the user dashboard
            }
        } catch (Exception e) {
            return;
        }

        tap(id(PORTAL_SWITCHER_CHIP));

        // Select "Attendee Portal" from the bottom sheet (item_portal_option.xml
        // → txtPortalName). Switches to DashboardActivity via the portal
        // switcher's switchToPortal().
        new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        AppiumBy.androidUIAutomator(
                                "new UiSelector().text(\"Attendee Portal\")")))
                .click();

        // Wait for the user dashboard (Request Event hub) to appear.
        waitForVisibleId(id(REQUEST_EVENT_BTN));
    }

    /* ── Seed flow: create a pending event request through the UI ──────── */

    /**
     * Self-seeds one pending event request.
     * <p>
     * Assumes the driver has just logged in as an ORGANIZER (whose landing is
     * the organizer portal {@code OrganizerDashboardActivity}). Because that
     * portal exposes no request-submission UI, we first switch to the Attendee
     * Portal (the user dashboard) which hosts the "Request Event" form
     * ({@code activity_user_dashboard.xml} → {@code btnNotificationsHub}).
     * When complete the driver is back on the user dashboard and the request
     * sits in the backend in PENDING state, visible on the admin Requests tab.
     *
     * @param eventName unique event name for the seeded request
     */
    public void seedEventRequest(String eventName) {
        switchToAttendeePortal();
        tapRequestEvent();
        // The submit button is below the fold inside the form ScrollView;
        // wait for a top-of-form field instead.
        waitForVisibleId(id(EVENT_NAME_INPUT));

        // Event Details
        scrollTo(id(EVENT_NAME_INPUT));
        type(id(EVENT_NAME_INPUT), eventName);
        selectEventCategory("Seminar");
        scrollTo(id(EVENT_DESCRIPTION_INPUT));
        type(id(EVENT_DESCRIPTION_INPUT), "Auto-generated event request for admin approval test");
        scrollTo(id(TARGET_AUDIENCE_INPUT));
        type(id(TARGET_AUDIENCE_INPUT), "Students");
        scrollTo(id(CAPACITY_INPUT));
        type(id(CAPACITY_INPUT), "100");

        // Schedule & Venue — all future dates (validated against "now")
        scrollTo(id(VENUE_INPUT));
        type(id(VENUE_INPUT), "Test Venue Building A");
        pickDateTime(id(START_DATE_TIME_INPUT), 3, 10, 0);
        pickDateTime(id(END_DATE_TIME_INPUT), 3, 12, 0);
        pickDateTime(id(REG_START_DATE_TIME_INPUT), 1, 9, 0);
        pickDateTime(id(REG_END_DATE_TIME_INPUT), 2, 17, 0);

        // Requester Details — typed explicitly so the prefill can't race us
        scrollTo(id(REQUESTER_NAME_INPUT));
        type(id(REQUESTER_NAME_INPUT), "Test Organizer");
        scrollTo(id(CONTACT_EMAIL_INPUT));
        type(id(CONTACT_EMAIL_INPUT), "testorganizer@test.com");
        scrollTo(id(CONTACT_NUMBER_INPUT));
        type(id(CONTACT_NUMBER_INPUT), "+639123456789");

        // Reason
        scrollTo(id(REASON_FOR_REQUEST_INPUT));
        type(id(REASON_FOR_REQUEST_INPUT), "Automated test seed");

        // Submit and wait for the success dialog, then return to dashboard
        scrollTo(id(SUBMIT_REQUEST_BUTTON));
        tap(id(SUBMIT_REQUEST_BUTTON));
        waitForVisibleId(id(SUCCESS_DASHBOARD_BUTTON));
        tap(id(SUCCESS_DASHBOARD_BUTTON));
    }

    /**
     * Navigates to Profile and taps Sign Out; waits for the login screen so
     * the driver can authenticate as another account.
     */
    public void signOut() {
        tapProfile();
        // Sign Out button sits below the fold inside the profile ScrollView
        scrollTo(id(BTN_PROFILE_LOGOUT));
        tap(id(BTN_PROFILE_LOGOUT));
        // MaterialAlertDialog confirmation — positive button is framework
        // android:id/button1 (rendered all-caps "SIGN OUT")
        new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                .until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.id("android:id/button1")))
                .click();
        waitForVisibleId(id("edtEmail"));
    }

    /* ── Private helpers ───────────────────────────────────────────────── */

    /**
     * Scrolls the field with {@code resourceId} into view inside the form's
     * ScrollView so Appium can interact with it (off-screen elements fail
     * visibility-based waits and typing).
     */
    private WebElement scrollTo(String resourceId) {
        String selector = "new UiScrollable(new UiSelector().scrollable(true))"
                + ".scrollIntoView(new UiSelector().resourceId(\""
                + resourceId + "\"))";
        return new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                .until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.androidUIAutomator(selector)));
    }

    /**
     * Taps the category dropdown, then selects the first option whose text
     * matches (popup list rendered by RequestEventActivity).
     */
    private void selectEventCategory(String category) {
        tap(id(CARD_EVENT_CATEGORY));
        new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        AppiumBy.androidUIAutomator("new UiSelector().text(\"" + category + "\")")))
                .click();
    }

    /**
     * Opens the native date/time picker for {@code fieldId}, advances the
     * calendar {@code monthsAhead} months, picks day 10 (or 1), then enters
     * the requested {@code hour}:{@code minute} in keyboard mode.
     */
    private void pickDateTime(String fieldId, int monthsAhead, int hour, int minute) {
        // The field settles below the fold after the previous picker closes
        scrollTo(fieldId);
        tap(fieldId);

        // ── Date picker ─────────────────────────────────────────────────
        new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_WAIT_SECONDS))
                .until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.id(ID_DATE_PICKER_HEADER)));

        for (int i = 0; i < monthsAhead; i++) {
            try {
                WebElement next = new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(ExpectedConditions.presenceOfElementLocated(
                                AppiumBy.id(ID_NEXT_MONTH)));
                next.click();
                Thread.sleep(150);
            } catch (Exception ignored) {
                break;
            }
        }

        // Pick day 10 of the current (navigated-to) month; fall back to day 1
        if (!clickEnabledText("10")) {
            clickEnabledText("1");
        }
        tapNativeOk();
        pause(300);

        // ── Time picker ─────────────────────────────────────────────────
        new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_WAIT_SECONDS))
                .until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.id(ID_TIME_HEADER)));

        setTime(hour, minute);
        tapNativeOk();
        pause(300);
    }

    private void setTime(int hour, int minute) {
        // Switch the stock TimePicker from clock mode to keyboard input mode.
        // (Icon button only exists in clock mode; in input mode there is a
        // different "Type in time" header button with id input_mode.)
        try {
            WebElement modeToggle = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.id(ID_INPUT_MODE_BUTTON)));
            modeToggle.click();
        } catch (Exception ignored) {
        }

        // 12-hour display: 13:00 -> "1", 12:00 -> "12", 0:00 -> "12"
        int hour12 = hour % 12 == 0 ? 12 : hour % 12;
        String desiredAmPm = hour >= 12 ? "PM" : "AM";

        // Enter hour and minute (fields exposed only in keyboard mode)
        try {
            WebElement hourInput = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.id(ID_INPUT_HOUR)));
            hourInput.clear();
            hourInput.sendKeys(String.valueOf(hour12));
        } catch (Exception ignored) {
        }
        try {
            WebElement minuteInput = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.id(ID_INPUT_MINUTE)));
            minuteInput.clear();
            minuteInput.sendKeys(String.format("%02d", minute));
        } catch (Exception ignored) {
        }

        setAmPm(desiredAmPm);
    }

    /**
     * Ensures the AM/PM spinner in the time picker matches {@code desired}
     * (e.g. hour 17 must become PM, else the time lands 12 hours off).
     */
    private void setAmPm(String desired) {
        try {
            WebElement spinner = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.id(ID_AM_PM_SPINNER)));
            spinner.click();
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.androidUIAutomator(
                                    "new UiSelector().text(\"" + desired + "\").clickable(true)")))
                    .click();
        } catch (Exception ignored) {
            // Non-fatal: defaults usually land on the right half anyway
        }
    }

    /** Clicks an enabled TextView whose full text equals {@code text}. */
    private boolean clickEnabledText(String text) {
        try {
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            AppiumBy.androidUIAutomator(
                                    "new UiSelector().text(\"" + text + "\").enabled(true)")));
            el.click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Taps a native dialog button (e.g. android:id/button1 = OK). */
    private void tapNativeOk() {
        new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        AppiumBy.id(ID_DIALOG_OK)))
                .click();
    }

    /** Sleep helper that swallows {@link InterruptedException}. */
    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}