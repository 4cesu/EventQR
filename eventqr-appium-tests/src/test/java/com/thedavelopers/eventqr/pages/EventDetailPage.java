package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Event Detail screen (TestFlow: ED-1..ED-7).
 * Layout: activity_event_detail.xml
 */
public class EventDetailPage extends BaseTest {

    // Top bar
    private static final String TOP_BAR = "layoutTopBar";
    private static final String BACK_BUTTON = "btnBack";

    // Hero
    private static final String EVENT_HERO = "layoutEventHero";
    private static final String EVENT_POSTER = "imgEventPosterHero";

    // Title / status row
    private static final String EVENT_TITLE = "txtDetailTitle";
    private static final String STATUS_LABEL = "txtDetailStatus";
    private static final String ORGANIZER = "txtOrganizer";

    // Description
    private static final String DESCRIPTION = "txtDetailDescription";

    // Info rows
    private static final String EVENT_DATE = "txtDetailDate";
    private static final String EVENT_TIME = "txtDetailTime";
    private static final String EVENT_VENUE = "txtDetailVenue";
    private static final String EVENT_CAPACITY = "txtDetailCapacity";

    // Registration status
    private static final String REG_PERCENT = "txtRegPercent";
    private static final String REG_PROGRESS = "pbRegistrationDetail";
    private static final String REMAINING_SPOTS = "txtRemainingSpots";

    // Rewards
    private static final String REWARDS_ROW = "layoutRewardsRow";
    private static final String REWARDS_AVAILABLE = "txtRewardsAvailable";

    // CTA
    private static final String REGISTER_BUTTON = "btnRegisterForEvent";

    // Skeleton
    private static final String SKELETON_TITLE = "skeletonDetailTitle";
    private static final String SKELETON_DESCRIPTION = "skeletonDetailDescription";

    public boolean isEventDetailVisible() {
        return isDisplayed(id(EVENT_TITLE));
    }

    public boolean isTopBarDisplayed() {
        return isDisplayed(id(TOP_BAR));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public String getEventTitle() {
        return getText(id(EVENT_TITLE));
    }

    public boolean isStatusLabelDisplayed() {
        return isDisplayed(id(STATUS_LABEL));
    }

    public boolean isPosterDisplayed() {
        return isDisplayed(id(EVENT_POSTER));
    }

    public boolean isDescriptionDisplayed() {
        return isDisplayed(id(DESCRIPTION));
    }

    public String getEventDate() {
        return getText(id(EVENT_DATE));
    }

    public String getEventTime() {
        return getText(id(EVENT_TIME));
    }

    public String getEventVenue() {
        return getText(id(EVENT_VENUE));
    }

    public String getEventCapacity() {
        return getText(id(EVENT_CAPACITY));
    }

    public boolean isRegistrationStatusDisplayed() {
        return isDisplayed(id(REG_PERCENT)) && isDisplayed(id(REG_PROGRESS))
                && isDisplayed(id(REMAINING_SPOTS));
    }

    public String getRemainingSpots() {
        return getText(id(REMAINING_SPOTS));
    }

    public boolean isRewardsRowDisplayed() {
        return isDisplayed(id(REWARDS_ROW));
    }

    public boolean isRewardsAvailableDisplayed() {
        return isDisplayed(id(REWARDS_AVAILABLE));
    }

    public boolean isRegisterButtonDisplayed() {
        return isDisplayed(id(REGISTER_BUTTON));
    }

    public void tapRegister() {
        tap(id(REGISTER_BUTTON));
    }

    public String getRegisterButtonText() {
        return getText(id(REGISTER_BUTTON));
    }

    public boolean isSkeletonDisplayed() {
        return isDisplayed(id(SKELETON_TITLE)) || isDisplayed(id(SKELETON_DESCRIPTION));
    }

    /**
     * The register CTA reflects availability (e.g. "Register",
     * "Registration Closed", "Approved", "Rejected").
     */
    public boolean isRegisterButtonLabel(String label) {
        return isTextDisplayed(label);
    }
}