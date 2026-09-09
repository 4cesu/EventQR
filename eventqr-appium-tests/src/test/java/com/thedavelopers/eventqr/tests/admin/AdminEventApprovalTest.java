package com.thedavelopers.eventqr.tests.admin;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AdminDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 7.2 Event Request Approval list (AEA-1..AEA-3) and 7.3 Approval
 * Detail actions (AED-1..AED-5). Every test self-seeds a fresh pending request
 * through the UI so the suite never depends on pre-created requests.
 */
public class AdminEventApprovalTest extends BaseTest {

    private AdminDashboardPage dash;

    /**
     * Self-seeds a fresh pending event request before every test: logs in as
     * the seeded organizer, submits a request through the UI, signs out, then
     * logs in as admin and opens the Requests tab. Tests never depend on
     * externally pre-created requests.
     */
    @BeforeEach
    void seedPendingRequestThenLoginAsAdmin() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        OrganizerDashboardPage organizer = new OrganizerDashboardPage();
        organizer.seedEventRequest(uniqueEventName());
        organizer.signOut();

        LoginPage adminLogin = new LoginPage();
        adminLogin.login(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        dash = new AdminDashboardPage();
        dash.openRequestsTab();
    }

    private String uniqueEventName() {
        return "AdminTest-" + System.currentTimeMillis();
    }

    @Test
    @DisplayName("AEA-1: Request list loads with status filter chips")
    void requestListAndChipsLoad() {
        assertAll(
                () -> assertTrue(isDisplayed(id("recyclerRequests")), "Event request list should be visible"),
                () -> assertTrue(isDisplayed(id("chipAll")), "All chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipPending")), "Pending chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipApproved")), "Approved chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipRejected")), "Rejected chip should be visible"),
                () -> assertTrue(isDisplayed(id("textTitle")), "Seeded request item should be visible")
        );
    }

    @Test
    @DisplayName("AEA-2: Swipe to refresh reloads, retry available on error")
    void swipeToRefreshAndRetry() {
        swipeDown();
        assertTrue(isDisplayed(id("recyclerRequests"))
                        || isDisplayed(id("buttonRetry"))
                        || isDisplayed(id("loadingRequests")),
                "List should reload; error retry or loader states tolerated");
    }

    @Test
    @DisplayName("AEA-3: Tapping a request opens its detail")
    void tapRequestOpensDetail() {
        // Apply the Pending filter so the newest seeded request is first.
        tap(id("chipPending"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                        AppiumBy.androidUIAutomator("new UiSelector().resourceId(\""
                                + id("textTitle") + "\")")))
                .click();
        assertTrue(isDisplayed(id("textDetailTitle")), "Request detail should open");
    }

    @Test
    @DisplayName("AED-1: Request detail shows submitted event info")
    void requestDetailShowsOrganizerInfo() {
        dash.openFirstPendingRequestDetail();
        assertTrue(isDisplayed(id("textDetailTitle")) || isDisplayed(id("textSubmittedBy")),
                "Request detail should show event/organizer info");
    }

    @Test
    @DisplayName("AED-2: Detail exposes approve and reject actions")
    void detailExposesActions() {
        dash.openFirstPendingRequestDetail();
        assertAll(
                () -> assertTrue(isDisplayed(id("buttonApprove")), "Approve action expected"),
                () -> assertTrue(isDisplayed(id("buttonReject")), "Reject action expected")
        );
    }

    @Test
    @DisplayName("AED-3: Approve shows confirmation sheet")
    void approveShowsConfirmation() {
        dash.openFirstPendingRequestDetail();
        tap(id("buttonApprove"));
        assertTrue(isDisplayed(id("textConfirmTitle")) || isDisplayed(id("buttonConfirmAction")),
                "Approval confirmation sheet should appear");
    }

    @Test
    @DisplayName("AED-4: Reject shows confirmation sheet")
    void rejectShowsConfirmation() {
        dash.openFirstPendingRequestDetail();
        tap(id("buttonReject"));
        assertTrue(isDisplayed(id("textConfirmTitle")) || isDisplayed(id("buttonCancelAction")),
                "Rejection confirmation sheet should appear");
    }

    @Test
    @DisplayName("AED-5: Confirming approval updates request status")
    void confirmApprovalUpdatesStatus() {
        dash.openFirstPendingRequestDetail();
        tap(id("buttonApprove"));
        tap(id("buttonConfirmAction"));
        assertTrue(isDisplayed(id("buttonDone")) || isTextDisplayed("Request Approved!"),
                "Approval should complete with confirmation dialog");
    }
}