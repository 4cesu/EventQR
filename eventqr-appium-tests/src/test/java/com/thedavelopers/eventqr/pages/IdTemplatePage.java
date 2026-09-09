package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * ID Template / ID Layout Configuration screen (TestFlow: IDT-1..IDT-6).
 * Resource IDs are set programmatically in IdTemplateSettingsActivity.kt
 * and declared in app/src/main/res/values/ids.xml.
 * Per TestFlow IDT-2, only field-visibility toggles are configurable.
 */
public class IdTemplatePage extends BaseTest {

    // Preview
    private static final String PREVIEW_CONTAINER = "idt_preview_container";

    // Toggles card
    private static final String TOGGLES_CARD = "idt_toggles_card";

    // Save
    private static final String SAVE_BUTTON = "idt_save_button";

    // Status
    private static final String STATUS_VIEW = "idt_status";

    // The toggle checkboxes are dynamically created per optional field;
    // use text-based locators for them since their count varies.
    private static final String TOGGLE_FIELD_ATTENDEE_ID = "Attendee ID";
    private static final String TOGGLE_FIELD_ROLE = "Role";
    private static final String TOGGLE_FIELD_EVENT_NAME = "Event Name";
    private static final String TOGGLE_FIELD_EVENT_DATE = "Event Date";

    public boolean isIdTemplateVisible() {
        return isDisplayed(id(TOGGLES_CARD)) || isDisplayed(id(PREVIEW_CONTAINER))
                || isDisplayed(id(SAVE_BUTTON));
    }

    public boolean isPreviewDisplayed() {
        return isDisplayed(id(PREVIEW_CONTAINER));
    }

    public boolean isTogglesCardDisplayed() {
        return isDisplayed(id(TOGGLES_CARD));
    }

    private void setToggleByLabel(String fieldLabel, boolean visible) {
        // Find the checkbox by text within the toggles card
        if (visible != isTextDisplayed(fieldLabel)) {
            tapByText(fieldLabel);
        }
    }

    public void setAttendeeIdVisible(boolean visible) {
        setToggleByLabel(TOGGLE_FIELD_ATTENDEE_ID, visible);
    }

    public void setRoleVisible(boolean visible) {
        setToggleByLabel(TOGGLE_FIELD_ROLE, visible);
    }

    public void setEventNameVisible(boolean visible) {
        setToggleByLabel(TOGGLE_FIELD_EVENT_NAME, visible);
    }

    public void setEventDateVisible(boolean visible) {
        setToggleByLabel(TOGGLE_FIELD_EVENT_DATE, visible);
    }

    public boolean isSaveButtonDisplayed() {
        return isDisplayed(id(SAVE_BUTTON));
    }

    public void tapSave() {
        tap(id(SAVE_BUTTON));
    }

    public boolean isSaveSuccessVisible() {
        return isDisplayed(id(STATUS_VIEW));
    }

    /**
     * Configures which fields are shown on the ID and saves.
     */
    public void configureTemplate(boolean attendeeId, boolean role, boolean eventName, boolean eventDate) {
        setAttendeeIdVisible(attendeeId);
        setRoleVisible(role);
        setEventNameVisible(eventName);
        setEventDateVisible(eventDate);
        tapSave();
    }
}
