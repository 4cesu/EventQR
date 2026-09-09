package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Staff My Assigned Events screen (TestFlow: SA-1..SA-7).
 * Layouts: activity_staff_assigned_events.xml, item_staff_assigned_event.xml
 */
public class StaffAssignedEventsPage extends BaseTest {

    private static final String SWIPE_REFRESH = "swipeRefreshAssignedEvents";
    private static final String RECYCLER = "recyclerAssignedEvents";
    private static final String EMPTY_MESSAGE = "txtAssignedEventsEmpty";

    // Item elements
    private static final String ITEM_DATE_LAYOUT = "layoutEventDate";
    private static final String ITEM_DAY = "txtEventDay";
    private static final String ITEM_MONTH = "txtEventMonth";
    private static final String ITEM_TITLE = "txtStaffEventTitle";
    private static final String ITEM_STATUS = "txtStaffEventStatus";
    private static final String ITEM_DATETIME = "txtStaffEventDateTime";
    private static final String ITEM_LOCATION = "txtStaffEventLocation";
    private static final String ITEM_SCAN_BUTTON = "btnScan";
    private static final String ITEM_ATTENDEES_BUTTON = "btnAttendees";

    // Bottom nav
    private static final String NAV_DASHBOARD = "navDashboard";
    private static final String NAV_SCANNER = "navScanner";
    private static final String NAV_EVENTS = "navEvents";
    private static final String NAV_LOGS = "navLogs";

    public boolean isAssignedEventsVisible() {
        return isDisplayed(id(NAV_EVENTS)) && (isDisplayed(id(RECYCLER))
                || isDisplayed(id(EMPTY_MESSAGE)));
    }

    public void refresh() {
        swipeDown();
    }

    public boolean isEmptyMessageDisplayed() {
        return isDisplayed(id(EMPTY_MESSAGE));
    }

    public boolean isEventItemDisplayed() {
        return isDisplayed(id(ITEM_TITLE));
    }

    public String getItemTitle() {
        return getText(id(ITEM_TITLE));
    }

    public String getItemDay() {
        return getText(id(ITEM_DAY));
    }

    public String getItemMonth() {
        return getText(id(ITEM_MONTH));
    }

    public String getItemDateTime() {
        return getText(id(ITEM_DATETIME));
    }

    public String getItemLocation() {
        return getText(id(ITEM_LOCATION));
    }

    public boolean isItemStatusDisplayed() {
        return isDisplayed(id(ITEM_STATUS));
    }

    public void tapScanButton() {
        tap(id(ITEM_SCAN_BUTTON));
    }

    public void tapAttendeesButton() {
        tap(id(ITEM_ATTENDEES_BUTTON));
    }

    public void openScannerTab() {
        tap(id(NAV_SCANNER));
    }

    public void openEventsTab() {
        tap(id(NAV_EVENTS));
    }

    public void openLogsTab() {
        tap(id(NAV_LOGS));
    }

    public void openDashboardTab() {
        tap(id(NAV_DASHBOARD));
    }
}