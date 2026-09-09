package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Event Request Details screen (TestFlow: ERD-1..ERD-5).
 * Layout: activity_attendee_event_request_detail.xml
 */
public class EventRequestDetailPage extends BaseTest {

    private static final String BACK_BUTTON = "btnBack";
    private static final String SWIPE_REFRESH = "swipeRefreshRequestDetail";

    private static final String ERROR_MESSAGE = "txtRequestDetailsError";
    private static final String RETRY_BUTTON = "btnRequestDetailsRetry";
    private static final String CONTENT = "contentRequestDetails";

    // Details
    private static final String TITLE = "txtDetailTitle";
    private static final String STATUS = "txtDetailStatus";
    private static final String DESCRIPTION = "txtDetailDescription";
    private static final String DATE = "txtDetailDate";
    private static final String LOCATION = "txtDetailLocation";
    private static final String CAPACITY = "txtDetailCapacity";
    private static final String SUBMITTED = "txtDetailSubmitted";

    // Admin note
    private static final String NOTE_CARD = "noteCard";
    private static final String NOTE_TITLE = "txtNoteTitle";
    private static final String NOTE_BODY = "txtNoteBody";

    public boolean isRequestDetailVisible() {
        return isDisplayed(id(TITLE));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
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

    public String getTitle() {
        return getText(id(TITLE));
    }

    public String getStatus() {
        return getText(id(STATUS));
    }

    public boolean isStatusLabel(String status) {
        return isTextDisplayed(status);
    }

    public boolean isDescriptionDisplayed() {
        return isDisplayed(id(DESCRIPTION));
    }

    public String getDescription() {
        return getText(id(DESCRIPTION));
    }

    public boolean isDateDisplayed() {
        return isDisplayed(id(DATE));
    }

    public boolean isLocationDisplayed() {
        return isDisplayed(id(LOCATION));
    }

    public boolean isCapacityDisplayed() {
        return isDisplayed(id(CAPACITY));
    }

    public boolean isSubmittedDisplayed() {
        return isDisplayed(id(SUBMITTED));
    }

    public boolean isNoteCardDisplayed() {
        return isDisplayed(id(NOTE_CARD));
    }

    public String getNoteTitle() {
        return getText(id(NOTE_TITLE));
    }

    public String getNoteBody() {
        return getText(id(NOTE_BODY));
    }
}