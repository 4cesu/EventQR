package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Staff Assignment screen (TestFlow: STA-1..STA-7).
 * Layouts: activity_staff_assignment.xml, item_staff_assignment.xml,
 * dialog_staff_assigned.xml
 */
public class StaffAssignmentPage extends BaseTest {

    private static final String BACK_BUTTON = "btnStaffAssignmentBack";
    private static final String ADD_STAFF_BUTTON = "btnAddStaff";

    private static final String SWIPE_REFRESH = "swipeRefreshStaffAssignment";
    private static final String RECYCLER = "recyclerStaffAssignment";
    private static final String LOADING = "progressStaffAssignment";
    private static final String EMPTY_MESSAGE = "txtEmptyStaff";

    // Item elements
    private static final String ITEM_AVATAR_INITIAL = "txtStaffAvatarInitial";
    private static final String ITEM_NAME = "txtStaffName";
    private static final String ITEM_EMAIL = "txtStaffEmail";
    private static final String ITEM_REMOVE = "txtRemoveStaff";

    // "Staff assigned" confirmation dialog
    private static final String DIALOG_DONE_BUTTON = "btnDoneAssigned";

    public boolean isStaffAssignmentVisible() {
        return isDisplayed(id(ADD_STAFF_BUTTON)) && isDisplayed(id(RECYCLER))
                || isDisplayed(id(EMPTY_MESSAGE));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public boolean isAddStaffButtonDisplayed() {
        return isDisplayed(id(ADD_STAFF_BUTTON));
    }

    public void tapAddStaff() {
        tap(id(ADD_STAFF_BUTTON));
    }

    public void refresh() {
        swipeDown();
    }

    public boolean isLoadingDisplayed() {
        return isDisplayed(id(LOADING));
    }

    public boolean isEmptyMessageDisplayed() {
        return isDisplayed(id(EMPTY_MESSAGE));
    }

    public boolean isStaffItemDisplayed() {
        return isDisplayed(id(ITEM_NAME));
    }

    public String getItemName() {
        return getText(id(ITEM_NAME));
    }

    public String getItemEmail() {
        return getText(id(ITEM_EMAIL));
    }

    public void tapRemoveStaff() {
        tap(id(ITEM_REMOVE));
    }

    public void confirmRemoval() {
        tapByText("Remove");
    }

    public void tapDoneAssigned() {
        tap(id(DIALOG_DONE_BUTTON));
    }

    public boolean isDoneAssignedDialogDisplayed() {
        return isDisplayed(id(DIALOG_DONE_BUTTON));
    }

    /**
     * Full add-staff flow: assigns the user by name (selected on the
     * search screen) and confirms the dialog.
     */
    public void confirmStaffAssigned() {
        tapDoneAssigned();
    }
}