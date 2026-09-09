package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Transaction Logs screen (TestFlow: TLOG-1..TLOG-6).
 * Resource IDs are set programmatically in TransactionLogsActivity.kt
 * and declared in app/src/main/res/values/ids.xml.
 */
public class TransactionLogsPage extends BaseTest {

    // Search
    private static final String SEARCH_FIELD = "tlg_search";

    // Summary row (stats cards)
    private static final String SUMMARY_CONTAINER = "tlg_summary";

    // Log list container
    private static final String LIST_CONTAINER = "tlg_list";

    // Detail container
    private static final String DETAIL_CONTAINER = "tlg_detail";

    // Filter chips row
    private static final String FILTER_ROW = "tlg_filter_row";

    public boolean isTransactionLogsVisible() {
        return isDisplayed(id(SEARCH_FIELD)) || isDisplayed(id(LIST_CONTAINER));
    }

    public boolean isSearchDisplayed() {
        return isDisplayed(id(SEARCH_FIELD));
    }

    public void enterSearch(String query) {
        type(id(SEARCH_FIELD), query);
    }

    public void clearSearch() {
        type(id(SEARCH_FIELD), "");
    }

    public boolean isSummaryDisplayed() {
        return isDisplayed(id(SUMMARY_CONTAINER));
    }

    public boolean isFilterRowDisplayed() {
        return isDisplayed(id(FILTER_ROW));
    }

    public void filterByStatus(String status) {
        tapByText(status);
    }

    public boolean isLogListDisplayed() {
        return isDisplayed(id(LIST_CONTAINER));
    }

    public boolean isLogItemDisplayed() {
        // Log cards are text-based within the list container
        return isDisplayed(id(LIST_CONTAINER));
    }

    public boolean isDetailDisplayed() {
        return isDisplayed(id(DETAIL_CONTAINER));
    }

    public boolean isEmptyMessageDisplayed() {
        return isTextDisplayed("No transaction logs match this view.");
    }

    public void refresh() {
        swipeDown();
    }
}
