package com.thedavelopers.eventqr.base;

import com.thedavelopers.eventqr.config.TestConfig;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public abstract class BaseTest {

    protected static AndroidDriver driver;
    protected static WebDriverWait wait;

    @BeforeEach
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setNoReset(false);
        options.setAutoGrantPermissions(true);
        options.setAvd(TestConfig.AVD_NAME);
        options.setApp(TestConfig.APK_PATH);
        options.setAppPackage(TestConfig.APP_PACKAGE);
        options.setAppActivity(TestConfig.APP_ACTIVITY);

        driver = new AndroidDriver(new URL(TestConfig.APPIUM_URL), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS));
        openAppToLoginScreen();
    }

    protected void openAppToLoginScreen() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_WAIT_SECONDS));
        try {
            longWait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.id(id("btnSignIn"))));
            tap(id("btnSignIn"));
        } catch (TimeoutException ignored) {
        }
        try {
            longWait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(id("edtEmail"))));
        } catch (TimeoutException ignored) {
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected String id(String shortId) {
        return TestConfig.APP_PACKAGE + ":id/" + shortId;
    }

    protected WebElement waitForId(String resourceId) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.id(resourceId)));
    }

    protected WebElement waitForVisibleId(String resourceId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(resourceId)));
    }

    protected WebElement waitForText(String text) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().text(\"" + text + "\")")));
    }

    protected WebElement waitForTextContains(String text) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"" + text + "\")")));
    }

    protected void tap(String resourceId) {
        waitForVisibleId(resourceId).click();
    }

    protected void tapByText(String text) {
        waitForText(text).click();
    }

    protected void type(String resourceId, String text) {
        WebElement el = waitForVisibleId(resourceId);
        el.clear();
        el.sendKeys(text);
    }

    protected String getText(String resourceId) {
        return waitForVisibleId(resourceId).getText();
    }

    protected boolean isDisplayed(String resourceId) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                    .until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(resourceId)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isTextDisplayed(String text) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                    .until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.androidUIAutomator("new UiSelector().text(\"" + text + "\")")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void swipeDown() {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.25);
        int endY = (int) (size.height * 0.75);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1);
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), startX, endY));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    protected void pressBack() {
        driver.navigate().back();
    }

    protected List<WebElement> findElements(String resourceId) {
        return driver.findElements(AppiumBy.id(resourceId));
    }
}
