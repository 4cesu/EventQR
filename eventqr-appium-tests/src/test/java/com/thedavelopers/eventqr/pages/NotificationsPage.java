package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Notifications screen (TestFlow: NOTIF-1..NOTIF-8).
 * Layouts: activity_notifications.xml, item_notification.xml
 */
public class NotificationsPage extends BaseTest {

    private static final String BACK_BUTTON = "btnBack";
    private static final String MARK_ALL_READ = "txtMarkAllRead";

    private static final String SWIPE_REFRESH = "swipeRefreshNotifications";
    private static final String RECYCLER = "recyclerNotifications";
    private static final String SKELETON = "skeletonLoading";

    private static final String EMPTY_MESSAGE = "txtNotificationsEmpty";
    private static final String ERROR_MESSAGE = "txtNotificationsError";
    private static final String RETRY_BUTTON = "btnNotificationsRetry";

    // Item elements
    private static final String ITEM_CARD = "layoutNotificationCard";
    private static final String ITEM_ICON_LAYOUT = "layoutNotificationIcon";
    private static final String ITEM_ICON = "imgNotificationIcon";
    private static final String ITEM_TITLE = "txtNotificationTitle";
    private static final String ITEM_MESSAGE = "txtNotificationMessage";
    private static final String ITEM_DATETIME = "txtNotificationDateTime";
    private static final String ITEM_UNREAD_DOT = "viewUnreadDot";

    public boolean isNotificationsVisible() {
        return isDisplayed(id(MARK_ALL_READ)) || isDisplayed(id(EMPTY_MESSAGE));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public boolean isMarkAllReadDisplayed() {
        return isDisplayed(id(MARK_ALL_READ));
    }

    public void tapMarkAllRead() {
        tap(id(MARK_ALL_READ));
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

    public boolean isNotificationItemDisplayed() {
        return isDisplayed(id(ITEM_TITLE));
    }

    public String getItemTitle() {
        return getText(id(ITEM_TITLE));
    }

    public String getItemMessage() {
        return getText(id(ITEM_MESSAGE));
    }

    public String getItemDateTime() {
        return getText(id(ITEM_DATETIME));
    }

    public boolean isUnreadDotDisplayed() {
        return isDisplayed(id(ITEM_UNREAD_DOT));
    }

    public void tapFirstNotification() {
        tap(id(ITEM_TITLE));
    }

    public boolean isNotificationVisibleOnScreen(String title) {
        return isTextDisplayed(title);
    }
}