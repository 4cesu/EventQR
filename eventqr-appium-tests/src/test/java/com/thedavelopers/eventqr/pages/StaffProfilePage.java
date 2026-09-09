package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Staff Profile screen (TestFlow: SP-1..SP-6).
 * Layout: activity_profile.xml (staff variant)
 */
public class StaffProfilePage extends BaseTest {

    private static final String SWIPE_REFRESH = "swipeRefreshProfile";

    // Header
    private static final String AVATAR_INITIAL = "txtProfileInitial";
    private static final String PROFILE_NAME = "txtProfileName";
    private static final String PROFILE_ROLE = "txtProfileRole";

    // Error / retry
    private static final String PROFILE_ERROR = "txtProfileError";
    private static final String RETRY_BUTTON = "btnProfileRetry";

    // Menu + detail card
    private static final String MENU_LAYOUT = "layoutProfileMenu";
    private static final String DETAIL_FULL_NAME = "txtProfileDetailName";
    private static final String DETAIL_EMAIL = "txtProfileDetailEmail";
    private static final String DETAIL_PHONE = "txtProfileDetailPhone";

    // Actions
    private static final String EDIT_PROFILE_ACTION = "btnEditProfile";
    private static final String CARD_TRANSACTION_HISTORY = "cardTransactionHistory";
    private static final String CARD_CLAIMED_REWARDS = "cardClaimedRewards";
    private static final String CARD_MY_EVENT_REQUESTS = "cardMyEventRequests";
    private static final String SIGN_OUT_BUTTON = "btnProfileLogout";

    // Bottom nav
    private static final String NAV_DASHBOARD = "navDashboard";
    private static final String NAV_EVENTS = "navEvents";
    private static final String NAV_REGISTERED = "navRegistered";
    private static final String NAV_REWARDS = "navRewards";
    private static final String NAV_PROFILE = "navProfile";

    public boolean isProfileVisible() {
        return isDisplayed(id(PROFILE_NAME)) && isDisplayed(id(AVATAR_INITIAL));
    }

    public void refresh() {
        swipeDown();
    }

    public String getAvatarInitial() {
        return getText(id(AVATAR_INITIAL));
    }

    public String getProfileName() {
        return getText(id(PROFILE_NAME));
    }

    public String getProfileRole() {
        return getText(id(PROFILE_ROLE));
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(id(PROFILE_ERROR));
    }

    public void tapRetry() {
        tap(id(RETRY_BUTTON));
    }

    public boolean isDetailFullNameDisplayed() {
        return isDisplayed(id(DETAIL_FULL_NAME));
    }

    public String getDetailFullName() {
        return getText(id(DETAIL_FULL_NAME));
    }

    public String getDetailEmail() {
        return getText(id(DETAIL_EMAIL));
    }

    public String getDetailPhone() {
        return getText(id(DETAIL_PHONE));
    }

    public boolean isEditProfileActionDisplayed() {
        return isDisplayed(id(EDIT_PROFILE_ACTION));
    }

    public void tapEditProfile() {
        tap(id(EDIT_PROFILE_ACTION));
    }

    public boolean isTransactionHistoryCardDisplayed() {
        return isDisplayed(id(CARD_TRANSACTION_HISTORY));
    }

    public void tapTransactionHistory() {
        tap(id(CARD_TRANSACTION_HISTORY));
    }

    public boolean isClaimedRewardsCardDisplayed() {
        return isDisplayed(id(CARD_CLAIMED_REWARDS));
    }

    public void tapClaimedRewards() {
        tap(id(CARD_CLAIMED_REWARDS));
    }

    public boolean isMyEventRequestsCardDisplayed() {
        return isDisplayed(id(CARD_MY_EVENT_REQUESTS));
    }

    public void tapMyEventRequests() {
        tap(id(CARD_MY_EVENT_REQUESTS));
    }

    public boolean isSignOutButtonDisplayed() {
        return isDisplayed(id(SIGN_OUT_BUTTON));
    }

    public void tapSignOut() {
        tap(id(SIGN_OUT_BUTTON));
    }

    public boolean isConfirmSignOutDialogDisplayed() {
        return isTextDisplayed("Sign Out") || isTextDisplayed("Sign out of your account?");
    }

    public void confirmSignOut() {
        tapByText("Sign Out");
    }

    public void openDashboardTab() {
        tap(id(NAV_DASHBOARD));
    }

    public void openEventsTab() {
        tap(id(NAV_EVENTS));
    }

    public void openProfileTab() {
        tap(id(NAV_PROFILE));
    }
}