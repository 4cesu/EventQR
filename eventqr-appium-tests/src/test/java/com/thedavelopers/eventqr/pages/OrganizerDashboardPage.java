package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

public class OrganizerDashboardPage extends BaseTest {

    private static final String NOTIFICATION_BELL = "imgNotification";
    private static final String EVENT_LIST = "recyclerEvents";
    private static final String MANAGER_EVENTS_BUTTON = "btnManageEvents";

    public boolean isDashboardVisible() {
        return isDisplayed(id("txtWelcome"));
    }

    public void tapNotificationBell() {
        tap(id(NOTIFICATION_BELL));
    }

    public boolean isEventListVisible() {
        return isDisplayed(id(EVENT_LIST));
    }

    public void tapManageEvents() {
        tap(id(MANAGER_EVENTS_BUTTON));
    }
}
