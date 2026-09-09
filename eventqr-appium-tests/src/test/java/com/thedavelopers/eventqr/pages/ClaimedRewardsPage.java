package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Claimed Rewards screen (TestFlow: CR-1..CR-6).
 * Layouts: activity_claimed_rewards.xml, item_claimed_reward.xml
 */
public class ClaimedRewardsPage extends BaseTest {

    private static final String BACK_BUTTON = "btnBack";
    private static final String SWIPE_REFRESH = "swipeRefreshClaimedRewards";
    private static final String RECYCLER = "recyclerClaimedRewards";
    private static final String SKELETON = "skeletonLoading";

    // Empty / error states
    private static final String EMPTY_MESSAGE = "txtClaimedRewardsEmpty";
    private static final String ERROR_MESSAGE = "txtClaimedRewardsError";
    private static final String RETRY_BUTTON = "btnClaimedRewardsRetry";

    // Item elements
    private static final String ITEM_GIFT_ICON = "imgClaimedGift";
    private static final String ITEM_TITLE = "txtClaimedTitle";
    private static final String ITEM_EVENT = "txtClaimedEvent";
    private static final String ITEM_DATE = "txtClaimedDate";
    private static final String ITEM_STATUS = "txtClaimedStatus";
    private static final String ITEM_POINTS = "txtClaimedPoints";

    public boolean isClaimedRewardsVisible() {
        return isDisplayed(id(RECYCLER)) || isDisplayed(id(EMPTY_MESSAGE));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public boolean isSkeletonDisplayed() {
        return isDisplayed(id(SKELETON));
    }

    public boolean isRecyclerDisplayed() {
        return isDisplayed(id(RECYCLER));
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

    public boolean isGiftIconDisplayed() {
        return isDisplayed(id(ITEM_GIFT_ICON));
    }

    public boolean isItemTitleDisplayed() {
        return isDisplayed(id(ITEM_TITLE));
    }

    public boolean isItemDisplayed() {
        return isGiftIconDisplayed() && isItemTitleDisplayed();
    }

    public String getItemTitle() {
        return getText(id(ITEM_TITLE));
    }

    public String getItemEvent() {
        return getText(id(ITEM_EVENT));
    }

    public String getItemDate() {
        return getText(id(ITEM_DATE));
    }

    public String getItemStatus() {
        return getText(id(ITEM_STATUS));
    }

    public boolean isItemPointsDisplayed() {
        return isDisplayed(id(ITEM_POINTS));
    }
}