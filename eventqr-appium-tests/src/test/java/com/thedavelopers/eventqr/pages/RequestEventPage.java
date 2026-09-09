package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Request Event Creation form (TestFlow: REQ-1..REQ-6).
 * Layout: activity_request_event.xml
 */
public class RequestEventPage extends BaseTest {

    private static final String BACK_BUTTON = "btnBack";
    private static final String BACK_TEXT = "backText";
    private static final String FORM_SCROLL = "formScrollView";
    private static final String FORM_MESSAGE = "formMessageText";

    // Event details
    private static final String EVENT_NAME_FIELD = "eventNameInput";
    private static final String CATEGORY_CARD = "cardEventCategory";
    private static final String SELECTED_CATEGORY = "txtSelectedEventCategory";
    private static final String CATEGORY_SPINNER = "spinnerEventCategory";
    private static final String DESCRIPTION_FIELD = "eventDescriptionInput";
    private static final String TARGET_AUDIENCE_FIELD = "targetAudienceInput";
    private static final String CAPACITY_FIELD = "capacityInput";
    private static final String VENUE_FIELD = "venueInput";

    // Schedule
    private static final String START_DATETIME_FIELD = "startDateTimeInput";
    private static final String END_DATETIME_FIELD = "endDateTimeInput";
    private static final String REGISTRATION_START_FIELD = "registrationStartDateTimeInput";
    private static final String REGISTRATION_END_FIELD = "registrationEndDateTimeInput";

    // Requester
    private static final String REQUESTER_NAME_FIELD = "requesterNameInput";
    private static final String CONTACT_EMAIL_FIELD = "contactEmailInput";
    private static final String CONTACT_NUMBER_FIELD = "contactNumberInput";
    private static final String REASON_FIELD = "reasonForRequestInput";

    // Poster
    private static final String POSTER_PICKER = "eventPosterPicker";
    private static final String POSTER_PREVIEW = "eventPosterPreview";
    private static final String POSTER_PLACEHOLDER = "eventPosterPlaceholder";
    private static final String POSTER_STATUS = "eventPosterStatusText";

    // Submit
    private static final String SUBMIT_PROGRESS = "submitProgress";
    private static final String SUBMIT_BUTTON = "submitRequestButton";
    private static final String CANCEL_BUTTON = "cancelButton";

    public boolean isRequestEventFormVisible() {
        return isDisplayed(id(EVENT_NAME_FIELD)) && isDisplayed(id(SUBMIT_BUTTON));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public void enterEventName(String name) {
        type(id(EVENT_NAME_FIELD), name);
    }

    /**
     * Picks the event category from the dropdown by visible label.
     */
    public void pickCategory(String category) {
        tap(id(CATEGORY_CARD));
        tapByText(category);
    }

    public void enterDescription(String description) {
        type(id(DESCRIPTION_FIELD), description);
    }

    public void enterTargetAudience(String audience) {
        type(id(TARGET_AUDIENCE_FIELD), audience);
    }

    public void enterCapacity(String capacity) {
        type(id(CAPACITY_FIELD), capacity);
    }

    public void enterVenue(String venue) {
        type(id(VENUE_FIELD), venue);
    }

    /**
     * Date/time inputs open native pickers; supply the final visible text if
     * required, else leave the fields to the pickers.
     */
    public void enterStartDateTime(String value) {
        type(id(START_DATETIME_FIELD), value);
    }

    public void enterEndDateTime(String value) {
        type(id(END_DATETIME_FIELD), value);
    }

    public void enterRegistrationStart(String value) {
        type(id(REGISTRATION_START_FIELD), value);
    }

    public void enterRegistrationEnd(String value) {
        type(id(REGISTRATION_END_FIELD), value);
    }

    public void enterRequesterName(String name) {
        type(id(REQUESTER_NAME_FIELD), name);
    }

    public void enterContactEmail(String email) {
        type(id(CONTACT_EMAIL_FIELD), email);
    }

    public void enterContactNumber(String number) {
        type(id(CONTACT_NUMBER_FIELD), number);
    }

    public void enterReason(String reason) {
        type(id(REASON_FIELD), reason);
    }

    /**
     * Fills the full form with sensible values; pass empty strings to keep a
     * field blank on purpose.
     */
    public void fillForm(String eventName, String category, String description,
                         String audience, String capacity, String venue,
                         String requesterName, String email, String number, String reason) {
        enterEventName(eventName);
        if (category != null && !category.isEmpty()) {
            pickCategory(category);
        }
        enterDescription(description);
        enterTargetAudience(audience);
        enterCapacity(capacity);
        enterVenue(venue);
        enterRequesterName(requesterName);
        enterContactEmail(email);
        enterContactNumber(number);
        enterReason(reason);
    }

    public boolean isPosterPickerDisplayed() {
        return isDisplayed(id(POSTER_PICKER));
    }

    public boolean isPosterPreviewDisplayed() {
        return isDisplayed(id(POSTER_PREVIEW));
    }

    public void tapSubmit() {
        tap(id(SUBMIT_BUTTON));
    }

    public boolean isSubmitProgressDisplayed() {
        return isDisplayed(id(SUBMIT_PROGRESS));
    }

    public void tapCancel() {
        tap(id(CANCEL_BUTTON));
    }

    public boolean isFormMessageDisplayed() {
        return isDisplayed(id(FORM_MESSAGE));
    }

    public String getFormMessage() {
        return getText(id(FORM_MESSAGE));
    }
}