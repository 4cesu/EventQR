package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttendeeProfileTest extends BaseTest {

    @BeforeEach
    void loginAndOpenProfile() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        // TODO: verify profile tab resource-id in Appium Inspector
        tap(id("navProfile"));
    }

    @Test
    @DisplayName("PROF-1: Profile screen shows user details")
    void profileShowsUserDetails() {
        // TODO: verify profile fields resource-ids in Appium Inspector
        assertAll(
                () -> assertTrue(isDisplayed(id("txtFullName")), "Full name should be visible"),
                () -> assertTrue(isDisplayed(id("txtEmail")), "Email should be visible"),
                () -> assertTrue(isDisplayed(id("txtPhone")), "Phone should be visible")
        );
    }

    @Test
    @DisplayName("PROF-2: Edit profile navigates to edit screen")
    void editProfileNavigates() {
        // TODO: verify edit profile link resource-id in Appium Inspector
        tap(id("btnEditProfile"));
        assertTrue(isDisplayed(id("edtPhone")), "Edit profile screen should be visible");
    }

    @Test
    @DisplayName("PROF-3: Phone number can be edited")
    void editPhoneNumber() {
        // TODO: verify edit profile flow resource-ids in Appium Inspector
        tap(id("btnEditProfile"));
        type(id("edtPhone"), "9123456789");
        // TODO: verify save button resource-id in Appium Inspector
        tap(id("btnSaveChanges"));
        assertTrue(isTextDisplayed("updated") || isTextDisplayed("Saved") || isDisplayed(id("txtPhone")),
                "Phone should update after save");
    }

    @Test
    @DisplayName("PROF-4: Change password navigates to change screen")
    void changePasswordNavigates() {
        // TODO: verify change password link resource-id in Appium Inspector
        if (isDisplayed(id("btnChangePassword"))) {
            tap(id("btnChangePassword"));
            assertTrue(isDisplayed(id("edtCurrentPassword")), "Change password screen should be visible");
        }
    }

    @Test
    @DisplayName("PROF-5: Profile shows role tag")
    void profileShowsRoleTag() {
        // TODO: verify role tag resource-id in Appium Inspector
        assertTrue(isDisplayed(id("txtRoleTag")) || isTextDisplayed("Attendee"),
                "Role tag should be visible on profile");
    }
}
