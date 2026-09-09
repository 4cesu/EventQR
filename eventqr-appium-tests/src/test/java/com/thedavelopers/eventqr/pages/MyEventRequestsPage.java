package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * My Event Requests list (TestFlow: MER-1..MER-5).
 * Layouts: activity_my_event_requests.xml, item_my_event_request.xml
 */
public class MyEventRequestsPage extends BaseTest {

    private static final String BACK_BUTTON = "btnBack";
    private static final String NEW_REQUEST_BUTTON = "btnNewRequest";

    private static final String SWIPE_REFRESH = "swipeRefreshMyEventRequests";
    private static final String RECYCLER = "recyclerMyEventRequests";
    private static final String SKELETON = "skeletonLoading";

    private static final String EMPTY_MESSAGE = "txtMyRequestsEmpty";
    private static final String ERROR_MESSAGE = "txtMyRequestsError";
    private static final String RETRY_BUTTON = "btnMyRequestsRetry";

    // Item elements
    private static final String ITEM_TITLE = "txtRequestTitle";
    private static final String ITEM_STATUS = "txtRequestStatus";
    private static final String ITEM_DESCRIPTION = "txtRequestDescription";
    private static final String ITEM_DATE = "txtRequestDate";
    private static final String ITEM_LOCATION = "txtRequestLocation";
    private static final String ITEM_SUBMITTED = "txtRequestSubmitted";

    public boolean isMyEventRequestsVisible() {
        return isDisplayed(id(NEW_REQUEST_BUTTON)) || isDisplayed(id(EMPTY_MESSAGE))
                || isDisplayed(id(RECYCLER));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public boolean isNewRequestButtonDisplayed() {
        return isDisplayed(id(NEW_REQUEST_BUTTON));
    }

    public void tapNewRequest() {
        tap(id(NEW_REQUEST_BUTTON));
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

    public boolean isRequestItemDisplayed() {
        return isDisplayed(id(ITEM_TITLE));
    }

    public String getItemTitle() {
        return getText(id(ITEM_TITLE));
    }

    public String getItemStatus() {
        return getText(id(ITEM_STATUS));
    }

    public String getItemDescription() {
        return getText(id(ITEM_DESCRIPTION));
    }

    public String getItemLocation() {
        return getText(id(ITEM_LOCATION));
    }

    public boolean isItemSubmittedDisplayed() {
        return isDisplayed(id(ITEM_SUBMITTED));
    }

    public void tapRequestItem() {
        tap(id(ITEM_TITLE));
    }

    public boolean isRequestStatusVisible(String status) {
        return isTextDisplayed(status);
    }
}