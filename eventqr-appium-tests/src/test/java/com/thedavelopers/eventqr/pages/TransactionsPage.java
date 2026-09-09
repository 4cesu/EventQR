package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Transaction History screen (TestFlow: TRANS-1..TRANS-6).
 * Layouts: activity_user_transaction_history.xml, item_transaction.xml
 */
public class TransactionsPage extends BaseTest {

    private static final String BACK_BUTTON = "btnBack";

    // Event filter
    private static final String FILTER_EVENT_LABEL = "txtFilterEventLabel";
    private static final String SELECTED_EVENT_CARD = "cardSelectedEvent";
    private static final String SELECTED_EVENT_TITLE = "txtSelectedEventTitle";
    private static final String SPINNER = "spinnerEventFilter";

    // Count
    private static final String HISTORY_COUNT = "txtHistoryTransactionCount";

    // List
    private static final String SWIPE_REFRESH = "swipeRefreshTransactions";
    private static final String RECYCLER = "recyclerTransactions";
    private static final String SKELETON = "skeletonLoading";

    // Empty / error
    private static final String EMPTY_MESSAGE = "txtTransactionsEmpty";
    private static final String ERROR_MESSAGE = "txtTransactionsError";
    private static final String RETRY_BUTTON = "btnTransactionsRetry";

    // Item elements
    private static final String ITEM_ICON_LAYOUT = "layoutTransactionIcon";
    private static final String ITEM_TREND_ICON = "imgTransactionTrend";
    private static final String ITEM_TITLE = "txtTransactionTitle";
    private static final String ITEM_EVENT = "txtTransactionEvent";
    private static final String ITEM_TIME = "txtTransactionTime";
    private static final String ITEM_POINTS = "txtTransactionPoints";
    private static final String ITEM_TAG = "txtTransactionTag";

    // Bottom nav
    private static final String NAV_DASHBOARD = "navDashboard";

    public boolean isTransactionHistoryVisible() {
        return isDisplayed(id(HISTORY_COUNT));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public boolean isEventFilterDisplayed() {
        return isDisplayed(id(SELECTED_EVENT_CARD));
    }

    public String getSelectedEventTitle() {
        return getText(id(SELECTED_EVENT_TITLE));
    }

    /**
     * Picks an event from the dropdown by visible label text.
     */
    public void pickEvent(String eventTitle) {
        tap(id(SELECTED_EVENT_CARD));
        tapByText(eventTitle);
    }

    public boolean isCountDisplayed() {
        return isDisplayed(id(HISTORY_COUNT));
    }

    public boolean isSkeletonDisplayed() {
        return isDisplayed(id(SKELETON));
    }

    public boolean isEmptyMessageDisplayed() {
        return isDisplayed(id(EMPTY_MESSAGE));
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(id(ERROR_MESSAGE));
    }

    public void tapRetry() {
        tap(id(RETRY_BUTTON));
    }

    public void refresh() {
        swipeDown();
    }

    public boolean isTransactionItemDisplayed() {
        return isDisplayed(id(ITEM_TITLE));
    }

    public String getItemTitle() {
        return getText(id(ITEM_TITLE));
    }

    public String getItemEvent() {
        return getText(id(ITEM_EVENT));
    }

    public String getItemTime() {
        return getText(id(ITEM_TIME));
    }

    public String getItemPoints() {
        return getText(id(ITEM_POINTS));
    }

    public String getItemTag() {
        return getText(id(ITEM_TAG));
    }

    public void tapFirstTransaction() {
        tap(id(ITEM_TITLE));
    }

    public boolean isItemTagVisible(String tag) {
        return isTextDisplayed(tag);
    }
}