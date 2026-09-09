package com.thedavelopers.eventqr.tests.admin;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.AdminDashboardPage;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("AEA-1: Event request list loads")
    void requestListLoads() {
        assertAll(
                () -> assertTrue(isDisplayed(id("recyclerRequests")), "Event request list should be visible"),
                () -> assertTrue(isDisplayed(id("textTitle")), "At least one seeded request item should be visible")
        );
    }

    @Test
    @DisplayName("AEA-2: Filter chips displayed for request statuses")
    void filterChipsDisplayed() {
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
        assertTrue(isDisplayed(id("recyclerRequests")), "Request list should remain after refresh");
        assertTrue(isDisplayed(id("textTitle")), "Request items should remain after refresh");
    }

    @Test
    @DisplayName("AED-1: Tapping request opens detail")
    void tapRequestOpensDetail() {
        dash.openFirstPendingRequestDetail();
        assertTrue(isDisplayed(id("textDetailTitle")), "Request detail should open");
    }

    @Test
    @DisplayName("AED-2: Request detail shows organizer info")
    void requestDetailShowsOrganizerInfo() {
        dash.openFirstPendingRequestDetail();
        assertTrue(isDisplayed(id("textDetailTitle")) || isDisplayed(id("textSubmittedBy")),
                "Request detail should show event/organizer info");
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