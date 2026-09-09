package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Edit Event Details screen (TestFlow: EED-1..EED-7).
 * Resource IDs are set programmatically in EditEventDetailsActivity.kt
 * and declared in app/src/main/res/values/ids.xml.
 */
public class EditEventPage extends BaseTest {

    // Form fields
    private static final String TITLE_FIELD = "eed_title_input";
    private static final String DESCRIPTION_FIELD = "eed_description_input";
    private static final String VENUE_FIELD = "eed_venue_input";
    private static final String CAPACITY_FIELD = "eed_capacity_input";

    // Banner section
    private static final String BANNER_PREVIEW = "eed_banner_preview";
    private static final String BANNER_STATUS = "eed_banner_status";
    private static final String BANNER_PICKER = "eed_banner_pick_button";

    // Schedule section
    private static final String SCHEDULE_CARD = "eed_schedule_card";
    private static final String REG_OPEN_VALUE = "eed_reg_open_value";
    private static final String REG_CLOSE_VALUE = "eed_reg_close_value";
    private static final String EVENT_END_VALUE = "eed_event_end_value";

    // Status + save
    private static final String STATUS_VIEW = "eed_status";
    private static final String SAVE_BUTTON = "eed_save_button";

    public boolean isEditEventVisible() {
        return isDisplayed(id(TITLE_FIELD)) || isDisplayed(id(SAVE_BUTTON))
                || isDisplayed(id(SCHEDULE_CARD));
    }

    public String getTitle() {
        return getText(id(TITLE_FIELD));
    }

    public void enterTitle(String title) {
        type(id(TITLE_FIELD), title);
    }

    public String getDescription() {
        return getText(id(DESCRIPTION_FIELD));
    }

    public void enterDescription(String description) {
        type(id(DESCRIPTION_FIELD), description);
    }

    public String getVenue() {
        return getText(id(VENUE_FIELD));
    }

    public void enterVenue(String venue) {
        type(id(VENUE_FIELD), venue);
    }

    public String getCapacity() {
        return getText(id(CAPACITY_FIELD));
    }

    public void enterCapacity(String capacity) {
        type(id(CAPACITY_FIELD), capacity);
    }

    public boolean isBannerPreviewDisplayed() {
        return isDisplayed(id(BANNER_PREVIEW));
    }

    public boolean isBannerStatusDisplayed() {
        return isDisplayed(id(BANNER_STATUS));
    }

    public void tapChooseBanner() {
        tap(id(BANNER_PICKER));
    }

    public boolean isScheduleSectionDisplayed() {
        return isDisplayed(id(SCHEDULE_CARD));
    }

    public boolean isRegWindowDisplayed() {
        return isDisplayed(id(REG_OPEN_VALUE));
    }

    public boolean isSaveButtonDisplayed() {
        return isDisplayed(id(SAVE_BUTTON));
    }

    public void tapSave() {
        tap(id(SAVE_BUTTON));
    }

    public boolean isSaveSuccessToastVisible(String message) {
        return isTextDisplayed(message);
    }
}
