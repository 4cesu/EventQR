package com.thedavelopers.eventqr.tests.navigation;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 3 — Global Navigation & Role Routing (NAV-PORTAL-*, NAV-ROLE-*)
 * plus the section-3 bottom-navigation model (NAV-BOT-*).
 *
 * <p>Role/portal model (TestFlow section 0): portals are NOT cumulative —
 * every user has the Attendee Portal plus EXACTLY their own role portal
 * (Staff / Organizer / Admin / Super Admin). A plain ATTENDEE only has the
 * Attendee Portal, so the portal-switcher chip stays hidden (NAV-PORTAL-1).
 * The switcher bottom sheet lists Attendee Portal + the user's own role
 * portal, each with icon + subtitle, and badges the current portal row.</p>
 *
 * <p>Resource IDs verified against EventQRMobile sources:
 * <ul>
 *   <li>chip: {@code portalSwitcherChip} (GONE when {@code portalsForRole}
 *       returns 1 portal — see Dashboard/Staff/Organizer/Admin setupPortalSwitcher)</li>
 *   <li>sheet rows: {@code item_portal_option.xml} → {@code txtPortalName},
 *       {@code txtPortalSubtitle}, {@code imgPortalIcon},
 *       {@code currentPortalBadge}; container {@code portalOptionsContainer}</li>
 *   <li>dashboards: {@code txtDashboardWelcome} (attendee), {@code txtStaffName}
 *       (staff), {@code txtHeaderTitle} (organizer), {@code textAdminPortalTitle}
 *       (admin; "Super Admin Portal" for SUPER_ADMIN)</li>
 * </ul></p>
 */
public class PortalSwitcherTest extends BaseTest {

    // ── Helpers ──────────────────────────────────────────────────────────

    private void performLogin(String email, String pass) {
        new LoginPage().login(email, pass);
    }

    /**
     * True when the portal-switcher chip is actually visible. The chip stays
     * in the layout for every dashboard but is set to {@code View.GONE} when
     * the role holds a single portal (plain attendee), so it is absent from
     * the accessibility tree in that case.
     */
    private boolean isPortalSwitcherVisible() {
        List<WebElement> chips = driver.findElements(AppiumBy.id(id("portalSwitcherChip")));
        return !chips.isEmpty() && chips.get(0).isDisplayed();
    }

    private void openPortalSwitcher() {
        tap(id("portalSwitcherChip"));
    }

    /** Taps a portal option row in the bottom sheet by its visible name. */
    private void tapPortalOption(String name) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().text(\"" + name + "\").clickable(true)")))
                .click();
    }

    /** Collects the portal names listed in the open bottom sheet. */
    private List<String> portalOptionNames() {
        return driver.findElements(AppiumBy.id(id("txtPortalName"))).stream()
                .map(WebElement::getText)
                .toList();
    }

    /**
     * Waits for the EventDetail register/manage CTA to leave its initial
     * "Loading..." state and returns the settled label (e.g. "Manage Event",
     * "Register", "Already Registered", "Can't verify registration").
     */
    private String awaitRegisterCtaSettled() {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            List<WebElement> buttons = driver.findElements(AppiumBy.id(id("btnRegisterForEvent")));
            if (!buttons.isEmpty()) {
                String label = buttons.get(0).getText();
                if (!"Loading...".equals(label)) {
                    return label;
                }
            }
            pause(300);
        }
        fail("Register CTA on EventDetail never left the 'Loading...' state");
        return null;
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    // ── NAV-PORTAL: portal switcher visibility & options ────────────────

    @Test
    @DisplayName("NAV-PORTAL-1: Plain attendee has NO portal switcher (attendee portal is default-only)")
    void attendeeHasNoPortalSwitcher() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("txtDashboardWelcome"));
        assertFalse(isPortalSwitcherVisible(),
                "Plain attendee must NOT see the portal switcher (TestFlow NAV-PORTAL-1)");
    }

    @Test
    @DisplayName("NAV-PORTAL-2: Staff portal switcher lists 'Staff Portal' (current, badged)")
    void staffSwitcherShowsStaffPortal() {
        performLogin(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        waitForVisibleId(id("txtStaffName"));
        assertTrue(isPortalSwitcherVisible(), "Staff dashboard shows the portal switcher");
        openPortalSwitcher();
        assertAll(
                () -> assertTrue(isTextDisplayed("Attendee Portal"), "Sheet lists Attendee Portal"),
                () -> assertTrue(isTextDisplayed("Staff Portal"), "Sheet lists Staff Portal"),
                () -> assertTrue(isDisplayed(id("currentPortalBadge")), "Current portal is badged")
        );
    }

    @Test
    @DisplayName("NAV-PORTAL-3: Organizer portal switcher lists 'Organizer Portal' (current, badged)")
    void organizerSwitcherShowsOrganizerPortal() {
        performLogin(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        waitForVisibleId(id("txtHeaderTitle"));
        assertTrue(isPortalSwitcherVisible(), "Organizer dashboard shows the portal switcher");
        openPortalSwitcher();
        assertAll(
                () -> assertTrue(isTextDisplayed("Attendee Portal"), "Sheet lists Attendee Portal"),
                () -> assertTrue(isTextDisplayed("Organizer Portal"), "Sheet lists Organizer Portal"),
                () -> assertTrue(isDisplayed(id("currentPortalBadge")), "Current portal is badged")
        );
    }

    @Test
    @DisplayName("NAV-PORTAL-4: Admin portal switcher lists 'Admin Portal' (current, badged)")
    void adminSwitcherShowsAdminPortal() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        waitForVisibleId(id("textAdminPortalTitle"));
        assertEquals("Admin Portal", getText(id("textAdminPortalTitle")), "Admin portal label");
        assertTrue(isPortalSwitcherVisible(), "Admin dashboard shows the portal switcher");
        openPortalSwitcher();
        assertAll(
                () -> assertTrue(isTextDisplayed("Attendee Portal"), "Sheet lists Attendee Portal"),
                () -> assertTrue(isTextDisplayed("Admin Portal"), "Sheet lists Admin Portal"),
                () -> assertTrue(isDisplayed(id("currentPortalBadge")), "Current portal is badged")
        );
    }

    // ── NAV-PORTAL: switching portals (TestFlow NAV-PORTAL-4..8) ─────────

    @Test
    @DisplayName("NAV-PORTAL-5: Switching to Staff Portal opens StaffDashboardActivity")
    void staffPortalSwitchOpensStaffDashboard() {
        performLogin(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        waitForVisibleId(id("txtStaffName"));

        // TestFlow NAV-PORTAL-4: Switching to Attendee Portal -> DashboardActivity.
        openPortalSwitcher();
        tapPortalOption("Attendee Portal");
        waitForVisibleId(id("txtDashboardWelcome"));

        // Session persists: chip still offered, no re-login required.
        assertTrue(isPortalSwitcherVisible(),
                "Staff keeps the switcher on the attendee dashboard (session preserved)");

        // TestFlow NAV-PORTAL-5: Switching to Staff Portal -> StaffDashboardActivity.
        openPortalSwitcher();
        tapPortalOption("Staff Portal");
        waitForVisibleId(id("txtStaffName"));
        assertTrue(isDisplayed(id("txtStaffName")),
                "Switching to Staff Portal opens the staff dashboard");
    }

    @Test
    @DisplayName("NAV-PORTAL-6: Switching to Organizer Portal opens OrganizerDashboardActivity")
    void organizerPortalSwitchOpensOrganizerDashboard() {
        performLogin(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        waitForVisibleId(id("txtHeaderTitle"));

        // TestFlow NAV-PORTAL-4: attendee hop.
        openPortalSwitcher();
        tapPortalOption("Attendee Portal");
        waitForVisibleId(id("txtDashboardWelcome"));

        // TestFlow NAV-PORTAL-6: Switching to Organizer Portal -> OrganizerDashboardActivity.
        openPortalSwitcher();
        tapPortalOption("Organizer Portal");
        waitForVisibleId(id("txtHeaderTitle"));
        assertEquals("Organizer Portal", getText(id("txtHeaderTitle")),
                "Switching to Organizer Portal opens the organizer dashboard");
    }

    @Test
    @DisplayName("NAV-PORTAL-7: Switching to Admin Portal opens AdminDashboardActivity")
    void adminPortalSwitchOpensAdminDashboard() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        waitForVisibleId(id("textAdminPortalTitle"));

        // TestFlow NAV-PORTAL-4: attendee hop.
        openPortalSwitcher();
        tapPortalOption("Attendee Portal");
        waitForVisibleId(id("txtDashboardWelcome"));

        // TestFlow NAV-PORTAL-7: Switching to Admin Portal -> AdminDashboardActivity.
        openPortalSwitcher();
        tapPortalOption("Admin Portal");
        waitForVisibleId(id("textAdminPortalTitle"));
        assertEquals("Admin Portal", getText(id("textAdminPortalTitle")),
                "Switching to Admin Portal opens the admin dashboard");
    }

    @Test
    @DisplayName("NAV-PORTAL-8: Super Admin switcher shows 'Super Admin Portal' (+ Attendee), not other roles' portals")
    void superAdminSwitcherShowsSuperAdminPortal() {
        // TestFlow SA-1/SA-2 + section 0: SUPER_ADMIN reuses the admin dashboard,
        // sees Attendee + exactly its OWN role portal (non-cumulative).
        performLogin(TestConfig.SUPERADMIN_EMAIL, TestConfig.SUPERADMIN_PASS);
        waitForVisibleId(id("textAdminPortalTitle"));
        assertEquals("Super Admin Portal", getText(id("textAdminPortalTitle")),
                "Super admin dashboard title names the Super Admin portal");
        assertTrue(isPortalSwitcherVisible(), "Super admin shows the portal switcher");
        assertTrue(getText(id("portalSwitcherChip")).contains("Super Admin"),
                "Switcher chip labels the Super Admin portal");

        openPortalSwitcher();
        List<String> options = portalOptionNames();
        assertAll(
                () -> assertTrue(options.contains("Attendee Portal"),
                        "Sheet lists Attendee Portal"),
                () -> assertTrue(options.contains("Super Admin Portal"),
                        "Sheet lists Super Admin Portal"),
                () -> assertFalse(options.contains("Admin Portal"),
                        "Portals are NOT cumulative: no Admin Portal option"),
                () -> assertFalse(options.contains("Organizer Portal"),
                        "Portals are NOT cumulative: no Organizer Portal option"),
                () -> assertFalse(options.contains("Staff Portal"),
                        "Portals are NOT cumulative: no Staff Portal option"),
                () -> assertTrue(isDisplayed(id("currentPortalBadge")), "Current portal is badged")
        );
    }

    // ── NAV-ROLE: role guards ────────────────────────────────────────────

    @Test
    @DisplayName("NAV-ROLE-1: Staff-only screens reject non-staff with 'Access Denied: Staff only'")
    void staffOnlyGuardRejectsNonStaff() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("btnNotificationsHub"));

        startActivity("com.thedavelopers.eventqr.features.staff.StaffDashboardActivity", null);
        assertTrue(isToastDisplayed("Access Denied: Staff only"),
                "Non-staff must get the 'Access Denied: Staff only' toast");

        // The guard finish()es the staff dashboard — the caller must remain.
        assertTrue(isDisplayed(id("btnNotificationsHub")) || isDisplayed(id("txtDashboardWelcome")),
                "Rejected non-staff must land back on the attendee dashboard");
        assertFalse(isPresent(id("txtStaffName")),
                "Staff dashboard must be finished for non-staff");
    }

    @Test
    @DisplayName("NAV-ROLE-2: EventDetail 'Manage Event' only for the event OWNER")
    void manageEventOnlyForOwner() {
        // ── Phase 1: event OWNER (organizer) sees "Manage Event" ─────────
        performLogin(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        OrganizerDashboardPage dash = new OrganizerDashboardPage();
        waitForVisibleId(id("txtHeaderTitle"));
        dash.seedApprovedEventIfNone();

        String eventTitle = getText(id("txtAttendeeEventTitle"));
        dash.switchToAttendeePortal();

        // Attendee-side Event Detail of the organizer's own approved event.
        tap(id("navEvents"));
        waitForVisibleId(id("inputEventSearch"));
        type(id("inputEventSearch"), eventTitle);
        if (driver.isKeyboardShown()) {
            pressBack(); // dismiss the keyboard so the filtered row is tappable
        }
        tapByText(eventTitle);
        waitForVisibleId(id("btnRegisterForEvent"));

        String ownerCta = awaitRegisterCtaSettled();
        assertEquals("Manage Event", ownerCta,
                "Event OWNER sees 'Manage Event' on EventDetail (ED-7)");

        tap(id("btnRegisterForEvent"));
        assertTrue(isTextDisplayed("Event Management"),
                "'Manage Event' must open EventManagementHubActivity");

        // Back out (hub -> detail -> events list -> dashboard) and sign out
        // so the same event can be checked as a non-owner.
        pressBack();
        pressBack();
        pressBack();
        waitForVisibleId(id("navProfile"));
        dash.signOut();
        waitForVisibleId(id("edtEmail"));

        // ── Phase 2: NON-OWNER (plain attendee) must NOT see "Manage Event" ──
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("navEvents"));
        tap(id("navEvents"));
        waitForVisibleId(id("inputEventSearch"));
        type(id("inputEventSearch"), eventTitle);
        if (driver.isKeyboardShown()) {
            pressBack(); // dismiss the keyboard so the filtered row is tappable
        }
        tapByText(eventTitle);
        waitForVisibleId(id("btnRegisterForEvent"));

        String nonOwnerCta = awaitRegisterCtaSettled();
        assertNotEquals("Manage Event", nonOwnerCta,
                "Non-owner must NOT see 'Manage Event' on EventDetail (NAV-ROLE-2)");
    }

    // ── NAV-BOT: bottom navigation per role (TestFlow section 3) ─────────

    @Test
    @DisplayName("NAV-BOT-1: Attendee bottom nav — Home, Events, Registered, Rewards, Profile")
    void attendeeBottomNav() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        waitForVisibleId(id("txtDashboardWelcome"));
        assertAll(
                () -> assertTrue(isDisplayed(id("navDashboard")), "Home nav item"),
                () -> assertTrue(isDisplayed(id("navEvents")), "Events nav item"),
                () -> assertTrue(isDisplayed(id("navRegistered")), "Registered nav item"),
                () -> assertTrue(isDisplayed(id("navRewards")), "Rewards nav item"),
                () -> assertTrue(isDisplayed(id("navProfile")), "Profile nav item")
        );
    }

    @Test
    @DisplayName("NAV-BOT-2: Staff bottom nav — Dashboard, Scan, Events, Logs")
    void staffBottomNav() {
        performLogin(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        waitForVisibleId(id("txtStaffName"));
        assertAll(
                () -> assertTrue(isDisplayed(id("navDashboard")), "Dashboard nav item"),
                () -> assertTrue(isDisplayed(id("navScanner")), "Scan nav item"),
                () -> assertTrue(isDisplayed(id("navEvents")), "Events nav item"),
                () -> assertTrue(isDisplayed(id("navLogs")), "Logs nav item")
        );
    }

    @Test
    @DisplayName("NAV-BOT-3: Organizer bottom nav — Dashboard, Events, Attendees, Logs, Reports, Rewards")
    void organizerBottomNav() {
        performLogin(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        waitForVisibleId(id("txtHeaderTitle"));
        assertAll(
                () -> assertTrue(isTextDisplayed("Dashboard"), "Dashboard nav item"),
                () -> assertTrue(isTextDisplayed("Events"), "Events nav item"),
                () -> assertTrue(isTextDisplayed("Attendees"), "Attendees nav item"),
                () -> assertTrue(isTextDisplayed("Logs"), "Logs nav item"),
                () -> assertTrue(isTextDisplayed("Reports"), "Reports nav item"),
                () -> assertTrue(isTextDisplayed("Rewards"), "Rewards nav item")
        );
    }

    @Test
    @DisplayName("NAV-BOT-4: Admin bottom nav — Dashboard, Requests, Accounts, Logs")
    void adminBottomNav() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        waitForVisibleId(id("textAdminPortalTitle"));
        assertAll(
                () -> assertTrue(isDisplayed(id("navDashboard")), "Dashboard nav item"),
                () -> assertTrue(isDisplayed(id("navRequests")), "Requests nav item"),
                () -> assertTrue(isDisplayed(id("navAccounts")), "Accounts nav item"),
                () -> assertTrue(isDisplayed(id("navLogs")), "Logs nav item")
        );
    }
}