package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage extends BaseTest {

    private static final String EMAIL_FIELD = "edtEmail";
    private static final String PASSWORD_FIELD = "edtPassword";
    private static final String SIGN_IN_BUTTON = "btnSignIn";
    private static final String FORGOT_PASSWORD_LINK = "txtForgotPassword";
    private static final String REGISTER_LINK = "btnRegister";

    protected AndroidDriver getDriver() {
        return driver;
    }

    protected WebDriverWait getWait() {
        return wait;
    }

    private String full(String shortId) {
        return id(shortId);
    }

    public void enterEmail(String email) {
        type(full(EMAIL_FIELD), email);
    }

    public void enterPassword(String password) {
        type(full(PASSWORD_FIELD), password);
    }

    public void tapSignIn() {
        tap(full(SIGN_IN_BUTTON));
    }

    public void tapForgotPassword() {
        tap(full(FORGOT_PASSWORD_LINK));
    }

    public void tapRegister() {
        tap(full(REGISTER_LINK));
    }

    /**
     * The login screen has no dedicated toggle button: LoginActivity wires an
     * OnTouchListener on edtPassword and toggles when the tap lands on the
     * drawableEnd region (right edge). Tap the right edge of the password field.
     */
    public void tapPasswordToggle() {
        hideKeyboard();
        WebElement field = waitForVisibleId(full(PASSWORD_FIELD));
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

    public void login(String email, String password) {
        clearFields();
        enterEmail(email);
        enterPassword(password);
        tapSignIn();
        waitForLoginToComplete();
    }

    /**
     * Polls until the driver leaves the login/landing/registration screens, i.e.
     * the post-login navigation completed (LoginActivity → LandingActivity token
     * refresh → role dashboard). Subsequent screen waits then start from a
     * rendered dashboard instead of racing the two network round-trips the app
     * performs after sign-in.
     */
    public void waitForLoginToComplete() {
        long deadline = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < deadline) {
            try {
                String current = getDriver().currentActivity();
                if (current != null
                        && !current.contains("LoginActivity")
                        && !current.contains("LandingActivity")
                        && !current.contains("RegistrationActivity")) {
                    return;
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new TimeoutException("Timed out waiting for post-login navigation to a role dashboard");
    }

    /**
     * Clears email and password fields so the LoginPage can be reused
     * without restarting the app (e.g. after signing out).
     */
    public void clearFields() {
        try {
            type(full(EMAIL_FIELD), "");
        } catch (Exception ignored) {
        }
        try {
            type(full(PASSWORD_FIELD), "");
        } catch (Exception ignored) {
        }
    }

    /**
     * The logo ImageView has no resource id; it is exposed via its
     * contentDescription "EventQR logo".
     */
    public boolean isLogoVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            AppiumBy.androidUIAutomator("new UiSelector().description(\"EventQR logo\")")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmailFieldVisible() {
        return isDisplayed(id(EMAIL_FIELD));
    }

    public boolean isPasswordFieldVisible() {
        return isDisplayed(id(PASSWORD_FIELD));
    }

    public boolean isSignInButtonVisible() {
        return isDisplayed(id(SIGN_IN_BUTTON));
    }

    public boolean isForgotPasswordLinkVisible() {
        return isDisplayed(id(FORGOT_PASSWORD_LINK));
    }

    /**
     * True when the password field content is plain-text visible (toggle ON).
     * UiAutomator2 exposes the "password" attribute: "true" when masked,
     * "false" when the input type is visible-password.
     */
    public boolean isPasswordVisibleAsPlainText() {
        try {
            WebElement pwdField = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            AppiumBy.id(full(PASSWORD_FIELD))));
            return "false".equals(pwdField.getAttribute("password"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * LoginPresenter sets EditText.error with "Enter a valid email address"
     * when the email is empty/malformed; there is no tilEmail on this screen.
     */
    public boolean isEmailFieldErrorVisible() {
        return isTextDisplayed("Enter a valid email address");
    }

    /**
     * LoginPresenter sets EditText.error with "Password must be at least 6
     * characters" when the password is short/empty.
     */
    public boolean isPasswordFieldErrorVisible() {
        return isTextDisplayed("Password must be at least 6 characters");
    }

    public boolean isFormErrorVisible() {
        return isTextDisplayed("Invalid email or password");
    }

    public boolean isSignInButtonEnabled() {
        return getWait().until(ExpectedConditions
                .presenceOfElementLocated(AppiumBy.id(full(SIGN_IN_BUTTON)))).isEnabled();
    }

    public WebElement signInButton() {
        return getWait().until(ExpectedConditions
                .visibilityOfElementLocated(AppiumBy.id(full(SIGN_IN_BUTTON))));
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