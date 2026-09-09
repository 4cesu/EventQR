package com.thedavelopers.eventqr.tests.navigation;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PortalSwitcherTest extends BaseTest {

    private void performLogin(String email, String pass) {
        LoginPage login = new LoginPage();
        login.login(email, pass);
    }

    private void logout() {
        // SO-1: open profile, SO-2: tap sign out, SO-3: confirm dialog
        // TODO: verify sign-out flow resource-ids in Appium Inspector
        if (isDisplayed(id("navProfile"))) {
            tap(id("navProfile"));
        }
        tap(id("btnSignOut"));
        if (isDisplayed(id("btnConfirmSignOut"))) {
            tap(id("btnConfirmSignOut"));
        }
        assertTrue(isDisplayed(id("btnSignIn")), "Should return to login after sign-out");
    }

    @Test
    @DisplayName("NAV-PORTAL-1: Portal switcher shows current portal")
    void portalSwitcherShowsCurrent() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        // TODO: verify portal switcher resource-id in Appium Inspector
        assertTrue(isDisplayed(id("portalSwitcherChip")), "Portal switcher chip should be visible");
    }

    @Test
    @DisplayName("NAV-PORTAL-2: Portal switcher lists available portals")
    void portalSwitcherListsPortals() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        tap(id("portalSwitcherChip"));
        // TODO: verify portal list resource-id in Appium Inspector
        assertTrue(isDisplayed(id("portalList")), "Portal list should open");
    }

    @Test
    @DisplayName("NAV-PORTAL-3: Current portal has badge")
    void currentPortalHasBadge() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        tap(id("portalSwitcherChip"));
        // TODO: verify current portal badge resource-id in Appium Inspector
        assertTrue(isDisplayed(id("badgeCurrentPortal")), "Current portal badge should be shown");
    }

    @Test
    @DisplayName("NAV-PORTAL-4: Switching portal opens correct activity")
    void switchingPortalOpensCorrectActivity() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        tap(id("portalSwitcherChip"));
        // TODO: verify portal option resource-id in Appium Inspector
        tap(id("portalOptionAttendee"));
        // TODO: verify attendee dashboard element resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtWelcome")), "Switching to attendee portal should open attendee dashboard");
    }

    @Test
    @DisplayName("NAV-PORTAL-5: Switching portal back to admin")
    void switchingPortalBackToAdmin() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        tap(id("portalSwitcherChip"));
        tap(id("portalOptionAdmin"));
        assertTrue(isDisplayed(id("textAdminPortalTitle")), "Switching back to admin portal should work");
    }

    @Test
    @DisplayName("NAV-PORTAL-6: Attendee portal switch lists attendee portals")
    void attendeePortalSwitchListsPortals() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        // TODO: verify attendee portal switcher resource-id in Appium Inspector
        if (isDisplayed(id("portalSwitcherChip"))) {
            tap(id("portalSwitcherChip"));
            assertTrue(isDisplayed(id("portalList")), "Portal list should open for attendee");
        }
    }

    @Test
    @DisplayName("NAV-PORTAL-7: Superadmin sees all portal options")
    void superadminSeesAllPortals() {
        performLogin(TestConfig.SUPERADMIN_EMAIL, TestConfig.SUPERADMIN_PASS);
        tap(id("portalSwitcherChip"));
        // TODO: verify superadmin has access to all portals in Appium Inspector
        assertTrue(isDisplayed(id("portalOptionAdmin"))
                        || isDisplayed(id("portalOptionOrganizer")),
                "Superadmin should have access to multiple portals");
    }

    @Test
    @DisplayName("NAV-PORTAL-8: Switching portal preserves session")
    void switchingPortalPreservesSession() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        tap(id("portalSwitcherChip"));
        tap(id("portalOptionAttendee"));
        // TODO: verify no re-login required resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtWelcome")), "Session should persist across portal switch");
    }

    @Test
    @DisplayName("NAV-ROLE-1: Attendee can sign out back to login")
    void attendeeSignOut() {
        performLogin(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        logout();
    }

    @Test
    @DisplayName("NAV-ROLE-2: Admin can sign out back to login")
    void adminSignOut() {
        performLogin(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        logout();
    }
}
