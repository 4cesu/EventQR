package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage extends BaseTest {

    private static final String EMAIL_FIELD = "edtEmail";
    private static final String PASSWORD_FIELD = "edtPassword";
    private static final String SIGN_IN_BUTTON = "btnSignIn";
    private static final String FORGOT_PASSWORD_LINK = "btnForgotPassword";
    private static final String REGISTER_LINK = "btnRegister";
    private static final String EMAIL_ERROR = "tilEmail";
    private static final String PASSWORD_ERROR = "tilPassword";

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

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        tapSignIn();
    }

    public boolean isEmailFieldErrorVisible() {
        // TODO: verify error resource-id in Appium Inspector
        return isDisplayed(id("txtEmailError"));
    }

    public boolean isPasswordFieldErrorVisible() {
        // TODO: verify error resource-id in Appium Inspector
        return isDisplayed(id("txtPasswordError"));
    }

    public boolean isFormErrorVisible() {
        return isTextDisplayed("Invalid credentials")
                || isTextDisplayed("Invalid email or password")
                || isTextDisplayed("Incorrect email or password");
    }

    public boolean isSignInButtonEnabled() {
        return getWait().until(ExpectedConditions
                .presenceOfElementLocated(AppiumBy.id(full(SIGN_IN_BUTTON)))).isEnabled();
    }

    public WebElement signInButton() {
        return getWait().until(ExpectedConditions
                .visibilityOfElementLocated(AppiumBy.id(full(SIGN_IN_BUTTON))));
    }
}
