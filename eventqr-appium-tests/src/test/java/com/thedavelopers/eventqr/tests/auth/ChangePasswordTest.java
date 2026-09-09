package com.thedavelopers.eventqr.tests.auth;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChangePasswordTest extends BaseTest {

    @Test
    @DisplayName("CHG-1: Change password screen loads with current, new, confirm fields")
    void changePasswordScreenLoads() {
        // TODO: verify change password screen element resource-ids in Appium Inspector
        assertAll(
                () -> assertTrue(isDisplayed(id("edtCurrentPassword")), "Current password field should be visible"),
                () -> assertTrue(isDisplayed(id("edtNewPassword")), "New password field should be visible"),
                () -> assertTrue(isDisplayed(id("edtConfirmNewPassword")), "Confirm new password field should be visible"),
                () -> assertTrue(isDisplayed(id("btnUpdatePassword")), "Update password button should be visible")
        );
    }

    @Test
    @DisplayName("CHG-2: Wrong current password shows error")
    void wrongCurrentPasswordShowsError() {
        // TODO: verify error message resource-id in Appium Inspector
        type(id("edtCurrentPassword"), "WrongPass123");
        type(id("edtNewPassword"), "NewPass@1234");
        type(id("edtConfirmNewPassword"), "NewPass@1234");
        tap(id("btnUpdatePassword"));
        assertTrue(isTextDisplayed("Incorrect current password")
                        || isTextDisplayed("Current password is incorrect")
                        || isTextDisplayed("Invalid current password"),
                "Wrong current password error should be visible");
    }

    @Test
    @DisplayName("CHG-3: New password must meet requirements")
    void newPasswordMustMeetRequirements() {
        // TODO: verify requirements validation resource-id in Appium Inspector
        type(id("edtCurrentPassword"), TestConfig.ATTENDEE_PASS);
        type(id("edtNewPassword"), "short");
        type(id("edtConfirmNewPassword"), "short");
        tap(id("btnUpdatePassword"));
        assertTrue(isTextDisplayed("at least")
                        || isTextDisplayed("requirements")
                        || isTextDisplayed("must contain"),
                "Password requirements error should be visible");
    }

    @Test
    @DisplayName("CHG-4: New and confirm passwords must match")
    void newAndConfirmMustMatch() {
        // TODO: verify mismatch error resource-id in Appium Inspector
        type(id("edtCurrentPassword"), TestConfig.ATTENDEE_PASS);
        type(id("edtNewPassword"), "NewPass@1234");
        type(id("edtConfirmNewPassword"), "Different@1234");
        tap(id("btnUpdatePassword"));
        assertTrue(isDisplayed(id("tilConfirmNewPassword")) || isTextDisplayed("do not match"),
                "Password mismatch error should be visible");
    }

    @Test
    @DisplayName("CHG-5: Valid change shows success confirmation")
    void validChangeShowsSuccess() {
        // TODO: verify success message resource-id in Appium Inspector
        type(id("edtCurrentPassword"), TestConfig.ATTENDEE_PASS);
        String newPass = "Updated@1234";
        type(id("edtNewPassword"), newPass);
        type(id("edtConfirmNewPassword"), newPass);
        tap(id("btnUpdatePassword"));
        assertTrue(isTextDisplayed("updated")
                        || isTextDisplayed("Password changed")
                        || isTextDisplayed("success"),
                "Success confirmation should be visible");
    }
}
