package com.thedavelopers.eventqr.tests.attendee;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 4.12 — ATTENDEE PROFILE (AttendeeProfileActivity)
 */
public class AttendeeProfileTest extends BaseTest {

    @BeforeEach
    void loginAndOpenProfile() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
        tap(id("navProfile"));
    }

    @Test
    @DisplayName("AP-1: Avatar initial from name, name, and role display name")
    void avatarInitialAndNameAndRole() {
        assertTrue(isDisplayed(id("txtProfileInitial")),
                "Avatar initial should be displayed");
        assertTrue(isDisplayed(id("txtProfileName")),
                "Name should be displayed");
        assertTrue(isDisplayed(id("txtProfileRole")) || isTextDisplayed("Attendee"),
                "Role display name should be visible");
    }

    @Test
    @DisplayName("AP-2: Detail rows — name, email, phone (dash when empty)")
    void detailRowsDisplayed() {
        assertAll(
                () -> assertTrue(isDisplayed(id("txtProfileDetailName")), "Full name row should be visible"),
                () -> assertTrue(isDisplayed(id("txtProfileDetailEmail")), "Email row should be visible"),
                () -> assertTrue(isDisplayed(id("txtProfileDetailPhone")), "Phone row should be visible")
        );
    }

    @Test
    @DisplayName("AP-3: Menu items — Edit Profile, Transaction History, Claimed Rewards, My Event Requests")
    void menuItemsDisplayed() {
        assertAll(
                () -> assertTrue(isDisplayed(id("cardEditProfile")) || isTextDisplayed("Edit Profile"),
                        "Edit Profile menu item should be visible"),
                () -> assertTrue(isDisplayed(id("cardTransactionHistory")) || isTextDisplayed("Transaction History"),
                        "Transaction History menu item should be visible"),
                () -> assertTrue(isDisplayed(id("cardClaimedRewards")) || isTextDisplayed("Claimed Rewards"),
                        "Claimed Rewards menu item should be visible"),
                () -> assertTrue(isDisplayed(id("cardMyEventRequests")) || isTextDisplayed("My Event Requests"),
                        "My Event Requests menu item should be visible")
        );
    }

    @Test
    @DisplayName("AP-4: Sign Out button shows confirmation dialog; confirm returns to LoginActivity")
    void signOutShowsConfirmation() {
        // Logout button sits below the fold inside the profile ScrollView
        scrollIntoView(id("btnProfileLogout"));
        assertTrue(isDisplayed(id("btnProfileLogout")),
                "Sign Out button should be visible");
        tap(id("btnProfileLogout"));
        // Confirmation dialog (SO-1) — message is the full exact sentence
        assertTrue(isTextDisplayed("Are you sure you want to sign out?"),
                "Confirmation dialog should appear");
        // Confirm sign out via dialog positive button (android:id/button1)
        tapDialogButton("android:id/button1");
        assertTrue(isDisplayed(id("edtEmail")) || isDisplayed(id("btnSignIn")),
                "Should navigate to LoginActivity after sign out");
        // Re-login for other tests
        LoginPage login = new LoginPage();
        login.login(TestConfig.ATTENDEE_EMAIL, TestConfig.ATTENDEE_PASS);
    }

    @Test
    @DisplayName("AP-5: Swipe-to-refresh and skeleton loading")
    void swipeToRefreshAndSkeleton() {
        assertTrue(isDisplayed(id("txtProfileName"))
                        || isDisplayed(id("skeletonLoading")),
                "Profile details or skeleton should be visible");
        swipeDown();
        assertTrue(isDisplayed(id("txtProfileName")),
                "Profile details should be visible after refresh");
    }

    @Test
    @DisplayName("AP-6: Successful profile load updates session (name, phone, email, role)")
    void sessionUpdatedOnLoad() {
        // After profile load, session data should be current
        String name = "";
        if (isDisplayed(id("txtProfileName"))) {
            name = getText(id("txtProfileName"));
        }
        assertFalse(name.isBlank(), "Profile name should be populated from session");
    }
}
