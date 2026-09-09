package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

public class AttendeeDashboardPage extends BaseTest {

    private static final String WELCOME_TEXT = "txtWelcome";
    private static final String NOTIFICATION_BELL = "imgNotification";
    private static final String SEE_ALL_EVENTS = "btnSeeAllEvents";
    private static final String BOTTOM_NAV_EVENTS = "navEvents";
    private static final String BOTTOM_NAV_REGISTERED = "navRegistered";
    private static final String BOTTOM_NAV_REWARDS = "navRewards";
    private static final String BOTTOM_NAV_PROFILE = "navProfile";

    public boolean isDashboardVisible() {
        return isDisplayed(id("txtWelcome"));
    }

    public String getWelcomeText() {
        return getText(id(WELCOME_TEXT));
    }

    public void tapNotificationBell() {
        tap(id(NOTIFICATION_BELL));
    }

    public void tapSeeAllEvents() {
        tap(id(SEE_ALL_EVENTS));
    }

    public void openEventsTab() {
        tap(id(BOTTOM_NAV_EVENTS));
    }

    public void openRegisteredTab() {
        tap(id(BOTTOM_NAV_REGISTERED));
    }

    public void openRewardsTab() {
        tap(id(BOTTOM_NAV_REWARDS));
    }

    public void openProfileTab() {
        tap(id(BOTTOM_NAV_PROFILE));
    }

    public boolean isStatsCardDisplayed(String resourceId) {
        return isDisplayed(resourceId);
    }
}
