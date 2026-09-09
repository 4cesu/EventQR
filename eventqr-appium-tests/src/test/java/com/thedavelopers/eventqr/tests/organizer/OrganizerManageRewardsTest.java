package com.thedavelopers.eventqr.tests.organizer;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 6.12 — Manage Rewards (MRW-1..MRW-5). Programmatic screen.
 * Rewards resolve their event from the persisted organizer selection
 * (resolveSelectedEvent saves the id), which the hub visit guarantees.
 * Creating a reward mutates the backend — names are unique per run.
 */
public class OrganizerManageRewardsTest extends BaseTest {

    private String createdReward;

    @BeforeEach
    void openRewards() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        OrganizerDashboardPage dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapSeeAllEvents();
        waitForVisibleId(id("recyclerEvents"));
        findElements(id("txtAttendeeEventTitle")).get(0).click();
        waitForText("Event Management");
        pressBack();
        dash.tapBottomNavLabel("Rewards");
        waitForText("Event Rewards");
    }

    @Test
    @DisplayName("MRW-1: Rewards screen renders with Add Reward")
    void rewardsScreenRenders() {
        assertAll(
                () -> assertTrue(isTextDisplayed("Event Rewards"), "Screen header expected"),
                () -> assertTrue(isTextDisplayed("Add Reward"), "Add Reward action expected")
        );
    }

    @Test
    @DisplayName("MRW-2: Reward dialog exposes the enable/disable control")
    void dialogExposesEnableControl() {
        tapByText("Add Reward");
        assertTrue(isTextDisplayed("Enable or disable reward redemption for this event"),
                "Enable/disable switch expected in the reward dialog");
        tapByText("Cancel");
    }

    @Test
    @DisplayName("MRW-3: Reward form fields render in the dialog")
    void dialogFieldsRender() {
        tapByText("Add Reward");
        assertAll(
                () -> assertTrue(isTextDisplayed("Reward Title"), "Reward Title field expected"),
                () -> assertTrue(isTextDisplayed("Points Cost"), "Points Cost field expected"),
                () -> assertTrue(isTextDisplayed("Total Quantity"), "Total Quantity field expected"),
                () -> assertTrue(isTextDisplayed("Allow duplicate claims"), "Duplicate claims switch expected"),
                () -> assertTrue(isTextDisplayed("Attendees may claim this reward more than once"),
                        "Duplicate claims hint expected")
        );
        tapByText("Cancel");
    }

    @Test
    @DisplayName("MRW-4: Create dialog is in create mode")
    void createDialogMode() {
        tapByText("Add Reward");
        assertTrue(isTextDisplayed("Create Reward"), "Create Reward dialog title expected");
        tapByText("Cancel");
    }

    @Test
    @DisplayName("MRW-5: Creating a reward persists across screen re-entry")
    void createRewardPersists() {
        createdReward = "Auto Reward " + System.currentTimeMillis();
        tapByText("Add Reward");
        waitForHint("Reward Title").sendKeys(createdReward);
        waitForHint("Points Cost").clear();
        waitForHint("Points Cost").sendKeys("25");
        tapByText("Create");
        assertTrue(isTextDisplayed(createdReward), "Created reward should appear in the list");

        // Re-enter the screen: the reward survives via the backend.
        pressBack(); // Rewards → Manage Events (bottom nav present there)
        waitForVisibleId(id("recyclerEvents"));
        tapByText("Rewards");
        waitForText("Event Rewards");
        assertTrue(isTextDisplayed(createdReward), "Reward should persist after re-entering the screen");
    }

    /* ── helpers ──────────────────────────────────────────────────────── */

    private org.openqa.selenium.WebElement waitForHint(String hint) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                AppiumBy.androidUIAutomator("new UiSelector().text(\"" + hint + "\")")));
    }
}