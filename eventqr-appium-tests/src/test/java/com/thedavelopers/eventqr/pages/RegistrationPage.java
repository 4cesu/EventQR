package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

public class RegistrationPage extends BaseTest {

    private static final String FIRST_NAME_FIELD = "edtFirstName";
    private static final String LAST_NAME_FIELD = "edtLastName";
    private static final String EMAIL_FIELD = "edtEmail";
    private static final String PHONE_FIELD = "edtPhoneNumber";
    private static final String PASSWORD_FIELD = "edtPassword";
    private static final String CONFIRM_PASSWORD_FIELD = "edtConfirmPassword";
    private static final String CREATE_ACCOUNT_BUTTON = "btnRegister";
    private static final String REQUIREMENTS_PANEL = "layoutPasswordRequirements";
    private static final String STRENGTH_BAR = "passwordStrengthBar";

    public void enterFullName(String name) {
        String[] parts = name.trim().split("\\s+", 2);
        type(id(FIRST_NAME_FIELD), parts[0]);
        if (parts.length > 1) {
            type(id(LAST_NAME_FIELD), parts[1]);
        }
    }

    public void enterEmail(String email) {
        type(id(EMAIL_FIELD), email);
    }

    public void enterPhone(String phone) {
        type(id(PHONE_FIELD), phone);
    }

    public void enterPassword(String password) {
        type(id(PASSWORD_FIELD), password);
    }

    public void enterConfirmPassword(String password) {
        type(id(CONFIRM_PASSWORD_FIELD), password);
    }

    public void tapCreateAccount() {
        tap(id(CREATE_ACCOUNT_BUTTON));
    }

    public String getPhoneValue() {
        return getText(id(PHONE_FIELD));
    }

    public boolean isRequirementsPanelVisible() {
        return isDisplayed(id(REQUIREMENTS_PANEL));
    }

    public boolean isStrengthBarVisible() {
        return isDisplayed(id(STRENGTH_BAR));
    }

    public int getStrengthBarCount() {
        // TODO: verify strength bar segment resource-id in Appium Inspector
        return findElements(id(STRENGTH_BAR)).size();
    }

    public boolean isCreateAccountButtonEnabled() {
        return findElements(id(CREATE_ACCOUNT_BUTTON)).stream()
                .findFirst()
                .map(e -> e.isEnabled())
                .orElse(false);
    }
}
