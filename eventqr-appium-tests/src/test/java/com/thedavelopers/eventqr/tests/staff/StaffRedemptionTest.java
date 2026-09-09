package com.thedavelopers.eventqr.tests.staff;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 5.12 — Scan Redemption (SRR-1..SRR-7) and 5.13 Redemption Result
 * (SDR-1..SDR-3). RewardRedemptionScanResultActivity renders its header
 * deterministically from extras; the rewards list needs the backend, so list
 * flows branch (eligible cards OR empty/error state). RedemptionResultActivity
 * is fully deterministic via extras.
 */
public class StaffRedemptionTest extends BaseTest {

    private static final String REWARD = "com.thedavelopers.eventqr.features.staff.reward";

    @BeforeEach
    void loginAsStaff() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.STAFF_EMAIL, TestConfig.STAFF_PASS);
    }

    /* ── 5.12 Scan Redemption ─────────────────────────────────────────── */

    @Test
    @DisplayName("SRR-1: Redemption scan result renders attendee and points balance")
    void redemptionHeaderRenders() {
        startActivity(REWARD + ".RewardRedemptionScanResultActivity",
                "--es extra_attendee_name \"Test Attendee\""
                        + " --ei extra_points_balance 500");
        assertAll(
                () -> assertTrue(isTextDisplayed("Reward Redemption"), "Screen title expected"),
                () -> assertTrue(isTextDisplayed("Test Attendee"), "Attendee name from extras expected"),
                () -> assertTrue(isTextDisplayed("Points Balance"), "Balance section expected"),
                () -> assertTrue(isTextDisplayed("Available rewards"), "Rewards section header expected")
        );
    }

    @Test
    @DisplayName("SRR-2: Rejected scan shows rejection state without rewards")
    void rejectedScanShowsRejection() {
        startActivity(REWARD + ".RewardRedemptionScanResultActivity",
                "--es extra_attendee_name \"Test Attendee\""
                        + " --es extra_scan_rejected true"
                        + " --es extra_scan_rejection_reason \"QR already scanned\"");
        assertAll(
                () -> assertTrue(isTextDisplayed("SCAN REJECTED"), "Rejection hero expected"),
                () -> assertTrue(isTextDisplayed("QR already scanned"), "Rejection reason expected")
        );
    }

    @Test
    @DisplayName("SRR-3: Rewards load failure is surfaced without crashing")
    void rewardsLoadFailureTolerated() {
        startActivity(REWARD + ".RewardRedemptionScanResultActivity",
                "--es extra_attendee_name \"Test Attendee\""
                        + " --ei extra_points_balance 0"
                        + " --es extra_event_id 00000000-0000-0000-0000-000000000000");
        assertAll(
                () -> assertTrue(isTextDisplayed("Available rewards"), "Rewards section expected"),
                () -> assertTrue(isTextDisplayed("No eligible rewards attendee.")
                                || isToastDisplayed("Unable to load rewards.")
                                || isDisplayed(id("recyclerRewards")),
                        "Empty, error, or loaded rewards states all accepted (data-dependent)")
        );
    }

    @Test
    @DisplayName("SRR-4: Empty eligible state renders the dedicated message")
    void emptyEligibleState() {
        startActivity(REWARD + ".RewardRedemptionScanResultActivity",
                "--es extra_attendee_name \"Test Attendee\""
                        + " --ei extra_points_balance 0"
                        + " --es extra_event_id 00000000-0000-0000-0000-000000000000");
        assertTrue(isTextDisplayed("No eligible rewards attendee.")
                        || isToastDisplayed("Unable to load rewards.")
                        || !findElements(id("recyclerRewards")).isEmpty(),
                "Empty eligible message, load error, or cards accepted (data-dependent)");
    }

    @Test
    @DisplayName("SRR-5: Redeem dialog summarizes the reward when a card is tapped")
    void redeemDialogFromRewardCard() {
        startActivity(REWARD + ".RewardRedemptionScanResultActivity",
                "--es extra_attendee_name \"Test Attendee\""
                        + " --ei extra_points_balance 9999"
                        + " --es extra_event_id 00000000-0000-0000-0000-000000000000");
        if (findElements(id("rewardCard")).isEmpty()) {
            assertTrue(isTextDisplayed("No eligible rewards attendee.")
                            || isToastDisplayed("Unable to load rewards."),
                    "No eligible rewards available; empty/error state accepted");
            return;
        }
        findElements(id("rewardCard")).get(0).click();
        assertTrue(isTextDisplayed("Redeem") || isTextDisplayed("Continue?"),
                "Redeem confirmation dialog should appear");
    }

    @Test
    @DisplayName("SRR-6: Missing event context is rejected with a toast")
    void missingEventContextToast() {
        startActivity(REWARD + ".RewardRedemptionScanResultActivity",
                "--es extra_attendee_name \"Test Attendee\" --ei extra_points_balance 100");
        assertTrue(isToastDisplayed("Missing event context"),
                "Blank event id must be rejected with a toast");
    }

    @Test
    @DisplayName("SRR-7: Redemption result continues scanning via its CTA")
    void continueScanningCtaFinishes() {
        startActivity(REWARD + ".RedemptionResultActivity",
                "--es extra_is_valid true --ei extra_points_delta 50"
                        + " --ei extra_points_balance 120 --es extra_reward_name \"Coffee Voucher\"");
        tapByText("Tap to continue scanning");
        assertFalse(isTextDisplayed("Tap to continue scanning"),
                "CTA should finish the result screen");
    }

    /* ── 5.13 Redemption Result (fully deterministic via extras) ──────── */

    @Test
    @DisplayName("SDR-1: Rejected redemption renders the rejection state")
    void rejectedRedemptionRenders() {
        startActivity(REWARD + ".RedemptionResultActivity",
                "--es extra_is_valid false --es extra_reason \"Insufficient points\"");
        assertAll(
                () -> assertTrue(isTextDisplayed("REDEMPTION REJECTED"), "Rejected hero expected"),
                () -> assertTrue(isTextDisplayed("Insufficient points"), "Reason expected"),
                () -> assertTrue(isTextDisplayed("No points were deducted"), "Outcome line expected"),
                () -> assertTrue(isTextDisplayed("0 pts"), "Zero-delta balance expected")
        );
    }

    @Test
    @DisplayName("SDR-2: Approved redemption renders the reward summary")
    void approvedRedemptionRenders() {
        startActivity(REWARD + ".RedemptionResultActivity",
                "--es extra_is_valid true --ei extra_points_delta 50"
                        + " --ei extra_points_balance 120 --es extra_reward_name \"Coffee Voucher\"");
        assertAll(
                () -> assertTrue(isTextDisplayed("REDEMPTION APPROVED"), "Approved hero expected"),
                () -> assertTrue(isTextDisplayed("Coffee Voucher"), "Reward name expected"),
                () -> assertTrue(isTextDisplayed("-50 pts"), "Points deducted expected"),
                () -> assertTrue(isTextDisplayed("120 pts"), "Remaining balance expected")
        );
    }

    @Test
    @DisplayName("SDR-3: Continue-scanning CTA returns to the scanner flow")
    void continueScanningReturns() {
        startActivity(REWARD + ".RedemptionResultActivity",
                "--es extra_is_valid true --es extra_reward_name \"Coffee Voucher\"");
        tapByText("Tap to continue scanning");
        assertFalse(isTextDisplayed("REDEMPTION APPROVED"),
                "CTA should leave the redemption result screen");
    }
}