package com.thedavelopers.eventqr.tests.staff;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 5.14 — Staff Profile (SP-1..SP-4).
 * StaffProfileActivity (features.staff.profile) has no dashboard entry in this
 * build, so tests drive it deterministically via am start. Bottom nav is the
 * staff profile variant (Dashboard / Scan QR / Logs / Profile; navRegistered
 * hidden).
 */
public class StaffProfileTest extends BaseTest {

    private static final String STAFF_PROFILE =
            "com.thedavelopers.eventqr.features.staff.profile.StaffProfileActivity";

    @BeforeEach
    void loginAsStaffAndOpenProfile() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
        startActivity(STAFF_PROFILE, null);
        waitForVisibleId(id("btnProfileLogout"));
    }

    @Test
    @DisplayName("SP-1: Staff profile renders identity and role")
    void profileRendersIdentity() {
        assertAll(
                () -> assertTrue(isDisplayed(id("txtProfileName")), "Profile name expected"),
                () -> assertTrue(isDisplayed(id("txtProfileRole")), "Profile role expected")
        );
    }

    @Test
    @DisplayName("SP-2: Sign out returns to the login screen")
    void signOutReturnsToLogin() {
        scrollToVisible(id("btnProfileLogout"));
        tap(id("btnProfileLogout"));
        waitForVisibleId("android:id/button1").click();
        waitForVisibleId(id("edtEmail"));
        assertTrue(isDisplayed(id("btnSignIn")), "Login screen should appear after sign out");
    }

    @Test
    @DisplayName("SP-3: Edit Profile opens the editable profile form")
    void editProfileOpensForm() {
        scrollToVisible(id("btnEditProfile"));
        tap(id("btnEditProfile"));
        assertAll(
                () -> assertTrue(isDisplayed(id("edtPhone")) || isDisplayed(id("edtFullName")),
                        "Editable fields expected"),
                () -> assertTrue(isDisplayed(id("btnSaveChanges")), "Save action expected")
        );
    }

    @Test
    @DisplayName("SP-4: Staff bottom nav hides the attendee-only Registered tab")
    void staffBottomNavConfiguration() {
        scrollToVisible(id("navDashboard"));
        assertAll(
                () -> assertTrue(isTextDisplayed("Dashboard"), "Dashboard label expected"),
                () -> assertTrue(isTextDisplayed("Scan QR"), "Scan QR label expected"),
                () -> assertTrue(isTextDisplayed("Logs"), "Logs label expected"),
                () -> assertTrue(isTextDisplayed("Profile"), "Profile label expected"),
                () -> assertFalse(isTextDisplayed("Registered"),
                        "navRegistered is hidden for staff profiles")
        );
    }

    private void scrollToVisible(String resourceId) {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView("
                                + "new UiSelector().resourceId(\"" + resourceId + "\"))")));
    }
}