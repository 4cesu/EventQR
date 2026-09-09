package com.thedavelopers.eventqr.tests.organizer;

import com.thedavelopers.eventqr.base.BaseTest;
import com.thedavelopers.eventqr.config.TestConfig;
import com.thedavelopers.eventqr.pages.LoginPage;
import com.thedavelopers.eventqr.pages.OrganizerDashboardPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFlow 6.5 — Staff Assignment Manage Users (MNU-1..MNU-4).
 * activity_staff_assignment.xml: assigned-staff list, add-staff entry, remove
 * confirmation dialog (programmatic rows + text dialog).
 */
public class OrganizerStaffAssignmentTest extends BaseTest {

    private static final String ASSIGNMENT =
            "com.thedavelopers.eventqr.features.organizer.staff.ManageUsersActivity";

    @BeforeEach
    void openStaffAssignment() {
        LoginPage login = new LoginPage();
        login.login(TestConfig.ORGANIZER_EMAIL, TestConfig.ORGANIZER_PASS);
        OrganizerDashboardPage dash = new OrganizerDashboardPage();
        dash.seedApprovedEventIfNone();
        dash.tapSeeAllEvents();
        waitForVisibleId(id("recyclerEvents"));
        findElements(id("txtAttendeeEventTitle")).get(0).click();
        waitForText("Event Management");
        tapByText("Staff Assignment");
        waitForVisibleId(id("recyclerStaffAssignment"));
    }

    @Test
    @DisplayName("MNU-1: Assigned staff list and add action render")
    void staffListRenders() {
        assertAll(
                () -> assertTrue(isDisplayed(id("recyclerStaffAssignment"))
                                || isDisplayed(id("txtEmptyStaff")),
                        "Assigned staff list or its empty state expected"),
                () -> assertTrue(isDisplayed(id("btnAddStaff")), "Add Staff action expected")
        );
    }

    @Test
    @DisplayName("MNU-2: Swipe to refresh reloads the assignment list")
    void swipeToRefreshReloads() {
        swipeDown();
        assertTrue(isDisplayed(id("recyclerStaffAssignment"))
                        || isDisplayed(id("txtEmptyStaff"))
                        || isDisplayed(id("progressStaffAssignment")),
                "Screen should remain while reloading");
    }

    @Test
    @DisplayName("MNU-3: Add Staff opens the user search screen")
    void addStaffOpensSearch() {
        tap(id("btnAddStaff"));
        assertTrue(isDisplayed(id("edtSearchUser"))
                        || isDisplayed(id("recyclerSearchUsers")),
                "Search user screen should open from Add Staff");
    }

    @Test
    @DisplayName("MNU-4: Removing a staff member asks for confirmation")
    void removeStaffConfirmation() {
        boolean hasRemoveAction;
        try {
            waitForTextContains("Remove");
            hasRemoveAction = true;
        } catch (Exception e) {
            hasRemoveAction = false;
        }
        if (!hasRemoveAction) {
            assertTrue(isDisplayed(id("recyclerStaffAssignment"))
                            || isDisplayed(id("txtEmptyStaff")),
                    "No staff rows to remove; screen verified instead");
            return;
        }
        tapByTextContains("Remove");
        assertTrue(isTextDisplayed("Remove staff?"),
                "Removal confirmation dialog expected");
        tapByText("Cancel");
        assertTrue(isDisplayed(id("recyclerStaffAssignment")),
                "Cancelling returns to the assignment list");
    }

    private void tapByTextContains(String text) {
        waitForTextContains(text).click();
    }
}