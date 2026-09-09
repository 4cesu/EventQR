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

    /**
     * Scrolls the view with the given full resource-id into view inside the
     * nearest scrollable container (ScrollView / NestedScrollView) so
     * visibility-based waits and taps work on below-the-fold elements.
     */
    protected WebElement scrollIntoView(String resourceId) {
        String selector = "new UiScrollable(new UiSelector().scrollable(true))"
                + ".scrollIntoView(new UiSelector().resourceId(\""
                + resourceId + "\"))";
        return new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_WAIT_SECONDS))
                .until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.androidUIAutomator(selector)));
    }

    /** Click a framework-resource dialog button by full id (e.g. "android:id/button1"). */
    protected void tapDialogButton(String frameworkId) {
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.id(frameworkId))).click();
    }

    /** Taps an absolute screen coordinate (physical pixels). */
    protected void tapAt(int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1);
        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(sequence));
    }

    /**
     * Taps the trailing (end) compound drawable of an EditText. The login and
     * registration screens render the password show/hide eye icon as an end
     * drawable on the field itself (app taps when
     * {@code rawX >= right - compoundPaddingEnd}), so there is no separate
     * button resource-id to click.
     */
    protected void tapFieldTrailingIcon(String resourceId) {
        WebElement el = waitForVisibleId(resourceId);
        org.openqa.selenium.Rectangle rect = el.getRect();
        int x = rect.getX() + rect.getWidth() - 24;
        int y = rect.getY() + rect.getHeight() / 2;
        tapAt(x, y);
    }

    /** True when a view with the given {@code content-desc} is visible. */
    protected boolean isContentDescDisplayed(String description) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_WAIT_SECONDS))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            AppiumBy.androidUIAutomator("new UiSelector().description(\"" + description + "\")")));
            return true;
        } catch (Exception e) {
            return false;
        }
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

    protected void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.75);
        int endY = (int) (size.height * 0.25);

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

    protected void hideKeyboard() {
        try {
            if (driver.isKeyboardShown()) {
                driver.hideKeyboard();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Polls {@code driver.currentActivity()} until it contains {@code activityName}
     * or {@link TestConfig#LONG_WAIT_SECONDS} elapse. Returns true when matched.
     */
    protected boolean waitForActivity(String activityName) {
        long deadline = System.currentTimeMillis() + TestConfig.LONG_WAIT_SECONDS * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                String current = driver.currentActivity();
                if (current != null && current.contains(activityName)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
return false;
    }

    protected List<WebElement> findElements(String resourceId) {
        return driver.findElements(AppiumBy.id(resourceId));
    }

    /**
     * Launches an activity via {@code mobile: startActivity} with an optional
     * extras string (e.g. {@code "--es extra_event_id <uuid> --es
     * extra_is_valid true"}). Lets tests drive deterministic screens
     * (result/redemption/detail screens) without crawling through multi-step
     * UI flows. The running user session's role gates apply exactly as in
     * production.
     *
     * <p>java-client 9.x dropped {@code AndroidDriver.startActivity(Activity)},
     * so this runs the underlying UiAutomator2 endpoint directly.</p>
     */
    protected void startActivity(String activityClassName, String optionalIntentArgs) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("package", TestConfig.APP_PACKAGE);
        params.put("activity", activityClassName);
        if (optionalIntentArgs != null && !optionalIntentArgs.isBlank()) {
            params.put("optionalIntentArguments", optionalIntentArgs);
        }
        driver.executeScript("mobile: startActivity", params);
    }

    /**
     * Restarts the app with a fresh process (like uninstalling/reinstalling for
     * session isolation within a single test) and lands on the login screen.
     */
    protected void restartAppToLogin() {
        driver.terminateApp(TestConfig.APP_PACKAGE);
        driver.activateApp(TestConfig.APP_PACKAGE);
        openAppToLoginScreen();
    }

    /**
     * True when a Toast with the given text is on screen. Toasts are LENGTH_LONG
     * (≈3.5 s) in this app, so a short dedicated wait usually catches them.
     */
    protected boolean isToastDisplayed(String text) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            AppiumBy.androidUIAutomator("new UiSelector().text(\"" + text + "\")")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** True when {@code resourceId} is present in the view hierarchy (visible or not). */
    protected boolean isPresent(String resourceId) {
        return !driver.findElements(AppiumBy.id(resourceId)).isEmpty();
    }

    /**
     * True when the view with {@code resourceId} carries a {@code content-invalid}
     * error, exposed by UiAutomator2 as an {@code error="..."} XML attribute
     * (Material EditText validation) rather than a separate text node, so
     * {@link #isTextDisplayed(String)} cannot see it.
     */
    protected boolean isFieldErrorDisplayed(String resourceId, String errorText) {
        try {
            String pageSource = driver.getPageSource();
            if (pageSource == null || !pageSource.contains(resourceId)) {
                return false;
            }
            int el = pageSource.indexOf(resourceId);
            int tagEnd = pageSource.indexOf('>', el);
            String node = pageSource.substring(el, tagEnd > 0 ? tagEnd : pageSource.length());
            return node.contains("content-invalid=\"true\"")
                    && node.contains("error=\"") 
                    && node.contains(errorText);
        } catch (Exception e) {
            return false;
        }
    }
}
