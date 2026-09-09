package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Organizer My Events (Manage Events) screen (TestFlow: ME-1..ME-6).
 * Layouts: activity_organizer_events.xml, item_event.xml
 */
public class ManageEventsPage extends BaseTest {

    private static final String TOOLBAR = "toolbarMyEvents";
    private static final String SEARCH_LAYOUT = "tilSearch";
    private static final String SEARCH_FIELD = "inputEventSearch";

    // Filter chips
    private static final String CHIP_ALL = "chipAll";
    private static final String CHIP_UPCOMING = "chipUpcoming";
    private static final String CHIP_ACTIVE = "chipActive";
    private static final String CHIP_COMPLETED = "chipCompleted";

    private static final String SWIPE_REFRESH = "swipeRefreshEvents";
    private static final String RECYCLER = "recyclerEvents";

    // States
    private static final String LOADING = "progressLoading";
    private static final String EMPTY_ICON = "imgEmptyIcon";
    private static final String EMPTY_MESSAGE = "txtEventsEmpty";
    private static final String EMPTY_SUB_MESSAGE = "txtEventsEmptySub";
    private static final String RETRY_BUTTON = "btnRefreshEvents";

    // Item elements
    private static final String ITEM_TITLE = "txtEventTitle";
    private static final String ITEM_STATUS = "txtEventStatus";
    private static final String ITEM_DETAILS = "txtEventDetails";

    public boolean isManageEventsVisible() {
        return isDisplayed(id(TOOLBAR)) && isDisplayed(id(SEARCH_FIELD));
    }

    public void search(String query) {
        type(id(SEARCH_FIELD), query);
    }

    public void clearSearch() {
        type(id(SEARCH_FIELD), "");
    }

    public void tapAllFilter() {
        tap(id(CHIP_ALL));
    }

    public void tapUpcomingFilter() {
        tap(id(CHIP_UPCOMING));
    }

    public void tapActiveFilter() {
        tap(id(CHIP_ACTIVE));
    }

    public void tapCompletedFilter() {
        tap(id(CHIP_COMPLETED));
    }

    public void refresh() {
        swipeDown();
    }

    public boolean isLoadingDisplayed() {
        return isDisplayed(id(LOADING));
    }

    public boolean isEmptyStateDisplayed() {
        return isDisplayed(id(EMPTY_MESSAGE));
    }

    public String getEmptyMessage() {
        return getText(id(EMPTY_MESSAGE));
    }

    public void tapRetry() {
        tap(id(RETRY_BUTTON));
    }

    public boolean isEventItemDisplayed() {
        return isDisplayed(id(ITEM_TITLE));
    }

    public String getItemTitle() {
        return getText(id(ITEM_TITLE));
    }

    public String getItemStatus() {
        return getText(id(ITEM_STATUS));
    }

    public boolean isItemDetailsDisplayed() {
        return isDisplayed(id(ITEM_DETAILS));
    }

    public boolean isEventWithStatusVisible(String status) {
        return isTextDisplayed(status);
    }

    public void tapEventItem() {
        tap(id(ITEM_TITLE));
    }
}