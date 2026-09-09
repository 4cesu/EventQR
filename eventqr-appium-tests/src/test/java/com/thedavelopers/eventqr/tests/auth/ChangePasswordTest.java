package com.thedavelopers.eventqr.tests.auth;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChangePasswordTest extends BaseTest {

    /**
     * Logs in as the seeded attendee and walks Profile -> Change Password.
     * The attendee session is required because CHG-3/CHG-5 hit the backend
     * change-password endpoint.
     */
    private void navigateToChangePassword() {
        type(id("edtEmail"), TestConfig.ATTENDEE_EMAIL);
        type(id("edtPassword"), TestConfig.ATTENDEE_PASS);
        tap(id("btnSignIn"));
        assertTrue(waitForActivity("features.dashboard.DashboardActivity"), "Should land on attendee dashboard");

        tap(id("navProfile"));
        assertTrue(waitForActivity("AttendeeProfileActivity"), "Should land on profile screen");

        // Profile content is a ScrollView; scroll until the Change Password row is visible.
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline && !isPresent(id("btnChangePassword"))) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for profile content");
            }
        }
        for (int i = 0; i < 4 && !isDisplayed(id("btnChangePassword")); i++) {
            swipeUp();
        }
        tap(id("btnChangePassword"));
        assertTrue(waitForActivity("ChangePasswordActivity"), "Should land on change password screen");
    }

    private void typeCurrentNewConfirm(String current, String newPass, String confirm) {
        type(id("edtCurrentPassword"), current);
        type(id("edtNewPassword"), newPass);
        type(id("edtConfirmPassword"), confirm);
    }

    @Test
    @DisplayName("CHG-1: Change password screen shows current, new, confirm fields and update button")
    void changePasswordScreenLoads() {
        navigateToChangePassword();
        assertAll(
                () -> assertTrue(isDisplayed(id("edtCurrentPassword")), "Current password field should be visible"),
                () -> assertTrue(isDisplayed(id("edtNewPassword")), "New password field should be visible"),
                () -> assertTrue(isDisplayed(id("edtConfirmPassword")), "Confirm new password field should be visible"),
                () -> assertTrue(isDisplayed(id("btnChangePassword")), "Change password button should be visible")
        );
    }

    @Test
    @DisplayName("CHG-2: New password shows live requirements indicators and gates the button")
    void newPasswordLiveRequirements() {
        navigateToChangePassword();
        typeCurrentNewConfirm(TestConfig.ATTENDEE_PASS, "short", "short");

        assertTrue(isDisplayed(id("layoutPasswordRequirements")),
                "Live requirements panel should appear while typing");
        assertTrue(isDisplayed(id("txtPasswordLengthRequirement")),
                "Length requirement indicator should be visible");
        assertFalse(isButtonEnabled(id("btnChangePassword")),
                "Change Password should be disabled while requirements unmet");

        typeCurrentNewConfirm(TestConfig.ATTENDEE_PASS, "NewPass@1234", "NewPass@1234");
        assertTrue(isButtonEnabled(id("btnChangePassword")),
                "Change Password should be enabled once all requirements are met");
    }

    @Test
    @DisplayName("CHG-3: Wrong current password shows error and stays on screen")
    void wrongCurrentPasswordShowsError() {
        navigateToChangePassword();
        typeCurrentNewConfirm("WrongPass123", "NewPass@1234", "NewPass@1234");
        tap(id("btnChangePassword"));
        assertTrue(isTextDisplayed("Current password is incorrect"),
                "Wrong current password error should be visible");
        assertTrue(waitForActivity("ChangePasswordActivity"), "Should stay on change password screen");
    }

    @Test
    @DisplayName("CHG-4: New and confirm passwords must match")
    void newAndConfirmMustMatch() {
        navigateToChangePassword();
        typeCurrentNewConfirm(TestConfig.ATTENDEE_PASS, "NewPass@1234", "Different@1234");
        tap(id("btnChangePassword"));
        assertTrue(isTextDisplayed("Passwords do not match"),
                "Password mismatch error should be visible");
        assertTrue(waitForActivity("ChangePasswordActivity"), "Should stay on change password screen");
    }

    @Test
    @DisplayName("CHG-5: Valid change shows success and returns to profile; seed password restored")
    void validChangeShowsSuccessAndRestoresPassword() {
        navigateToChangePassword();
        typeCurrentNewConfirm(TestConfig.ATTENDEE_PASS, "Updated@1234", "Updated@1234");
        tap(id("btnChangePassword"));
        assertTrue(isTextDisplayed("Password changed successfully"),
                "Success confirmation should be visible");
        assertTrue(waitForActivity("AttendeeProfileActivity"), "Should navigate back to profile");

        // Restore the seeded password so other suites/tests keep working on reruns.
        tap(id("btnChangePassword"));
        assertTrue(waitForActivity("ChangePasswordActivity"), "Should reopen change password screen");
        typeCurrentNewConfirm("Updated@1234", TestConfig.ATTENDEE_PASS, TestConfig.ATTENDEE_PASS);
        tap(id("btnChangePassword"));
        assertTrue(isTextDisplayed("Password changed successfully"),
                "Restore confirmation should be visible");
        assertTrue(waitForActivity("AttendeeProfileActivity"), "Should navigate back to profile after restore");
    }

    private boolean isButtonEnabled(String resourceId) {
        return findElements(resourceId).stream()
                .findFirst()
                .map(e -> e.isEnabled())
                .orElse(false);
    }
}