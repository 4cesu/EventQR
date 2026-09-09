package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RegistrationPage extends BaseTest {

    private static final String FIRST_NAME_FIELD = "edtFirstName";
    private static final String LAST_NAME_FIELD = "edtLastName";
    private static final String EMAIL_FIELD = "edtEmail";
    private static final String PHONE_FIELD = "edtPhoneNumber";
    private static final String PASSWORD_FIELD = "edtPassword";
    private static final String CONFIRM_PASSWORD_FIELD = "edtConfirmPassword";
    private static final String TERMS_CHECKBOX = "chkTerms";
    private static final String CREATE_ACCOUNT_BUTTON = "btnRegister";
    private static final String REQUIREMENTS_PANEL = "layoutPasswordRequirements";
    private static final String STRENGTH_BAR = "passwordStrengthBar";
    private static final String PHONE_COUNTER = "txtPhoneCounter";

    protected AndroidDriver getDriver() {
        return driver;
    }

    protected WebDriverWait getWait() {
        return wait;
    }

    public void enterFirstName(String firstName) {
        type(id(FIRST_NAME_FIELD), firstName);
    }

    public void enterLastName(String lastName) {
        type(id(LAST_NAME_FIELD), lastName);
    }

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

    /**
     * Taps the Terms &amp; Conditions checkbox. Hides the soft keyboard first so the
     * checkbox (below confirm password) is not obscured after field entry.
     */
    public void tapTermsCheckbox() {
        hideKeyboard();
        tap(id(TERMS_CHECKBOX));
    }

    public boolean isTermsCheckboxDisplayed() {
        return isDisplayed(id(TERMS_CHECKBOX));
    }

    public void tapCreateAccount() {
        tap(id(CREATE_ACCOUNT_BUTTON));
    }

    /**
     * RegistrationActivity toggles password visibility via an OnTouchListener
     * on the field: the toggle is the drawableEnd region (right edge). No
     * dedicated toggle button id exists.
     */
    public void tapPasswordToggle() {
        hideKeyboard();
        tapRightEdgeOf(PASSWORD_FIELD);
    }

    public void tapConfirmPasswordToggle() {
        hideKeyboard();
        tapRightEdgeOf(CONFIRM_PASSWORD_FIELD);
    }

    private void tapRightEdgeOf(String fieldShortId) {
        WebElement field = getWait().until(ExpectedConditions
                .visibilityOfElementLocated(AppiumBy.id(id(fieldShortId))));
        org.openqa.selenium.Rectangle rect = field.getRect();
        int x = rect.getX() + rect.getWidth() - 30;
        int y = rect.getY() + rect.getHeight() / 2;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "toggle");
        Sequence seq = new Sequence(finger, 1);
        seq.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        seq.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        seq.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        getDriver().perform(List.of(seq));
    }

    public String getPhoneValue() {
        return getText(id(PHONE_FIELD));
    }

    public String getPhoneCounterText() {
        return getText(id(PHONE_COUNTER));
    }

    public boolean isRequirementsPanelVisible() {
        return isDisplayed(id(REQUIREMENTS_PANEL));
    }

    public boolean isStrengthBarVisible() {
        return isDisplayed(id(STRENGTH_BAR));
    }

    public int getStrengthBarCount() {
        return findElements(id(STRENGTH_BAR)).size();
    }

    public boolean isCreateAccountButtonEnabled() {
        return findElements(id(CREATE_ACCOUNT_BUTTON)).stream()
                .findFirst()
                .map(e -> e.isEnabled())
                .orElse(false);
    }

    /**
     * True when the password field content is plain-text visible (toggle ON).
     */
    public boolean isPasswordVisibleAsPlainText() {
        return isFieldPlainText(PASSWORD_FIELD);
    }

    /**
     * True when the confirm password field content is plain-text visible (toggle ON).
     */
    public boolean isConfirmPasswordVisibleAsPlainText() {
        return isFieldPlainText(CONFIRM_PASSWORD_FIELD);
    }

    private boolean isFieldPlainText(String fieldShortId) {
        try {
            WebElement field = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.id(id(fieldShortId))));
            return "false".equals(field.getAttribute("password"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Field errors below map to RegistrationPresenter messages rendered through
     * EditText.error popups (no TextInputLayout error ids on this screen).
     */
    public boolean isFirstNameErrorVisible() {
        return isTextDisplayed("First name is required");
    }

    public boolean isLastNameErrorVisible() {
        return isTextDisplayed("Last name is required");
    }

    public boolean isEmailErrorVisible() {
        return isTextDisplayed("Enter a valid email address");
    }

    public boolean isPhoneErrorVisible() {
        return isTextDisplayed("Enter valid 10-digit mobile number");
    }

    public boolean isPasswordErrorVisible() {
        return isTextDisplayed("Password must meet all requirements");
    }

    public boolean isConfirmPasswordErrorVisible() {
        return isTextDisplayed("Passwords do not match");
    }

    /**
     * True when the current foreground activity matches the given class name.
     */
    public boolean isCurrentActivity(String activityName) {
        try {
            String currentActivity = driver.currentActivity();
            return currentActivity != null && currentActivity.contains(activityName);
        } catch (Exception e) {
            return false;
        }
    }
}