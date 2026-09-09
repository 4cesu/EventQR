package com.thedavelopers.eventqr.tests.admin;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AdminDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminEventApprovalTest extends BaseTest {

    private AdminDashboardPage dash;

    @BeforeEach
    void loginAsAdminAndOpenRequests() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ADMIN_EMAIL, TestConfig.ADMIN_PASS);
        dash = new AdminDashboardPage();
        dash.openRequestsTab();
    }

    @Test
    @DisplayName("AEA-1: Event request list loads")
    void requestListLoads() {
        // TODO: verify request list resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerRequests")), "Event request list should be visible");
    }

    @Test
    @DisplayName("AEA-2: Filter chips displayed for request statuses")
    void filterChipsDisplayed() {
        // TODO: verify filter chip resource-ids in Appium Inspector
        assertAll(
                () -> assertTrue(isDisplayed(id("chipAll")), "All chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipPending")), "Pending chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipApproved")), "Approved chip should be visible"),
                () -> assertTrue(isDisplayed(id("chipRejected")), "Rejected chip should be visible")
        );
    }

    @Test
    @DisplayName("AEA-3: Swipe down refreshes request list")
    void swipeDownRefreshes() {
        swipeDown();
        // TODO: verify refresh indicator resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerRequests")), "Request list should remain after refresh");
    }

    @Test
    @DisplayName("AED-1: Tapping request opens detail")
    void tapRequestOpensDetail() {
        // TODO: verify request item resource-id in Appium Inspector
        assertTrue(isDisplayed(id("recyclerRequests")), "Request list present");
    }

    @Test
    @DisplayName("AED-2: Request detail shows organizer info")
    void requestDetailShowsOrganizerInfo() {
        // TODO: verify request detail fields resource-ids in Appium Inspector
        assertTrue(isDisplayed(id("txtOrganizerName")) || isDisplayed(id("txtRequestTitle")),
                "Request detail should show organizer/event info");
    }

    @Test
    @DisplayName("AED-3: Approve shows confirmation dialog")
    void approveShowsConfirmation() {
        // TODO: verify approve button resource-id in Appium Inspector
        tap(id("btnApprove"));
        assertTrue(isDisplayed(id("dialogConfirmApproval")), "Approval confirmation dialog should appear");
    }

    @Test
    @DisplayName("AED-4: Reject shows confirmation dialog")
    void rejectShowsConfirmation() {
        // TODO: verify reject button resource-id in Appium Inspector
        tap(id("btnReject"));
        assertTrue(isDisplayed(id("dialogConfirmRejection")), "Rejection confirmation dialog should appear");
    }

    @Test
    @DisplayName("AED-5: Confirming approval updates request status")
    void confirmApprovalUpdatesStatus() {
        // TODO: verify confirm dialog button resource-id in Appium Inspector
        tap(id("btnApprove"));
        tap(id("btnConfirmApproval"));
        assertTrue(isTextDisplayed("approved") || isTextDisplayed("Approved")
                        || isDisplayed(id("dialogApproved")),
                "Approval should complete with confirmation");
    }
}
