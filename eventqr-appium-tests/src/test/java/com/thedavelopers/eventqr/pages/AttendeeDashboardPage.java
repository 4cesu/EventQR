package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

public class AttendeeDashboardPage extends BaseTest {

    // Header — real IDs from activity_user_dashboard.xml / DashboardActivity.kt
    private static final String WELCOME_TEXT = "txtDashboardWelcome";
    private static final String NAME_TEXT = "txtDashboardName";

    // Notification bell
    private static final String NOTIFICATION_BELL = "btnDashboardNotifications";
    private static final String UNREAD_DOT = "viewNotificationDot";

    // See All
    private static final String SEE_ALL_EVENTS = "txtDiscoverEventsSeeAll";

    // Upcoming section
    private static final String UPCOMING_SECTION = "layoutUpcomingEvents";

    // Discover section
    private static final String DISCOVER_SECTION = "layoutDiscoverEvents";

    // Stats — real IDs from activity_user_dashboard.xml
    private static final String STAT_EVENTS = "txtTotalEvents";
    private static final String STAT_REGISTRATIONS = "txtTotalRegistrations";
    private static final String STAT_COMPLETED = "txtTotalCompleted";

    // Hub cards
    private static final String ATTENDEE_HUB_CARD = "btnAttendeeHub";
    private static final String TRANSACTION_HISTORY_CARD = "btnTransactionHistory";
    private static final String NOTIFICATIONS_HUB_CARD = "btnNotificationsHub";

    // Event card elements — real IDs from item_attendee_event.xml
    private static final String EVENT_CARD_TITLE = "txtAttendeeEventTitle";
    private static final String EVENT_CARD_STATUS = "txtAttendeeEventStatus";
    private static final String EVENT_CARD_DATE_DAY = "txtEventDay";
    private static final String EVENT_CARD_DATE_MONTH = "txtEventMonth";
    private static final String EVENT_CARD_TIME = "txtAttendeeEventDateTime";
    private static final String EVENT_CARD_LOCATION = "txtAttendeeEventLocation";
    private static final String EVENT_CARD_CAPACITY = "txtRegistrationCount";
    private static final String EVENT_CARD_PROGRESS = "pbRegistration";

    // Bottom nav — real IDs from activity_user_dashboard.xml
    private static final String BOTTOM_NAV_HOME = "navDashboard";
    private static final String BOTTOM_NAV_EVENTS = "navEvents";
    private static final String BOTTOM_NAV_REGISTERED = "navRegistered";
    private static final String BOTTOM_NAV_REWARDS = "navRewards";
    private static final String BOTTOM_NAV_PROFILE = "navProfile";

    // Skeleton
    private static final String SKELETON_CONTAINER = "skeletonLoading";

    public boolean isDashboardVisible() {
        return isDisplayed(id(WELCOME_TEXT));
    }

    public String getWelcomeText() {
        return getText(id(WELCOME_TEXT));
    }

    public String getNameText() {
        return getText(id(NAME_TEXT));
    }

    public boolean isNotificationBellDisplayed() {
        return isDisplayed(id(NOTIFICATION_BELL));
    }

    public boolean isUnreadDotDisplayed() {
        return isDisplayed(id(UNREAD_DOT));
    }

    public void tapNotificationBell() {
        tap(id(NOTIFICATION_BELL));
    }

    public void tapSeeAllEvents() {
        tap(id(SEE_ALL_EVENTS));
    }

    public void tapAttendeeHub() {
        tap(id(ATTENDEE_HUB_CARD));
    }

    public void tapTransactionHistory() {
        tap(id(TRANSACTION_HISTORY_CARD));
    }

    public void tapNotificationsHub() {
        tap(id(NOTIFICATIONS_HUB_CARD));
    }

    public boolean isUpcomingSectionDisplayed() {
        return isDisplayed(id(UPCOMING_SECTION));
    }

    public boolean isUpcomingEmptyDisplayed() {
        return isTextDisplayed("No upcoming events yet.");
    }

    public boolean isDiscoverSectionDisplayed() {
        return isDisplayed(id(DISCOVER_SECTION));
    }

    public boolean isDiscoverEmptyDisplayed() {
        return isTextDisplayed("No discoverable events right now.");
    }

    public boolean isStatsCardDisplayed() {
        return isDisplayed(id(STAT_EVENTS))
                || isDisplayed(id(STAT_REGISTRATIONS))
                || isDisplayed(id(STAT_COMPLETED));
    }

    public boolean isEventCardDisplayed() {
        return isDisplayed(id(EVENT_CARD_TITLE));
    }

    public boolean isEventCardStatusDisplayed() {
        return isDisplayed(id(EVENT_CARD_STATUS));
    }

    public boolean isEventCardDateDisplayed() {
        return isDisplayed(id(EVENT_CARD_DATE_DAY)) && isDisplayed(id(EVENT_CARD_DATE_MONTH));
    }

    public boolean isEventCardTimeDisplayed() {
        return isDisplayed(id(EVENT_CARD_TIME));
    }

    public boolean isEventCardLocationDisplayed() {
        return isDisplayed(id(EVENT_CARD_LOCATION));
    }

    public boolean isEventCardCapacityDisplayed() {
        return isDisplayed(id(EVENT_CARD_CAPACITY));
    }

    public boolean isEventCardProgressDisplayed() {
        return isDisplayed(id(EVENT_CARD_PROGRESS));
    }

    public void tapEventCard() {
        tap(id(EVENT_CARD_TITLE));
    }

    public void openHomeTab() {
        tap(id(BOTTOM_NAV_HOME));
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

    public boolean isBottomNavDisplayed() {
        return isDisplayed(id(BOTTOM_NAV_EVENTS))
                && isDisplayed(id(BOTTOM_NAV_REGISTERED))
                && isDisplayed(id(BOTTOM_NAV_REWARDS))
                && isDisplayed(id(BOTTOM_NAV_PROFILE));
    }

    public boolean isSkeletonDisplayed() {
        return isDisplayed(id(SKELETON_CONTAINER));
    }

    public boolean isHomeTabHighlighted() {
        return isDisplayed(id(BOTTOM_NAV_HOME));
    }
}
