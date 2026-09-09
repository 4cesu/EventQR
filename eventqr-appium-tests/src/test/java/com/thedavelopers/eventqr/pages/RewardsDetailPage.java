package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Reward Details screen (TestFlow: RD-1..RD-5).
 * Layout: activity_user_reward_details.xml
 */
public class RewardsDetailPage extends BaseTest {

    private static final String BACK_BUTTON = "btnBack";
    private static final String REWARD_TITLE = "txtRewardTitle";
    private static final String POINTS_VALUE = "txtPointsValue";
    private static final String REWARD_STATUS = "txtRewardStatus";
    private static final String REWARD_REMAINING = "txtRewardRemaining";
    private static final String USER_POINTS = "txtUserPoints";
    private static final String WARNING_BOX = "warningBox";
    private static final String WARNING_MESSAGE = "txtWarningMessage";

    public boolean isRewardDetailVisible() {
        return isDisplayed(id(REWARD_TITLE));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public String getRewardTitle() {
        return getText(id(REWARD_TITLE));
    }

    public boolean isPointsValueDisplayed() {
        return isDisplayed(id(POINTS_VALUE));
    }

    public boolean isStatusDisplayed() {
        return isDisplayed(id(REWARD_STATUS));
    }

    public String getStatus() {
        return getText(id(REWARD_STATUS));
    }

    public boolean isRemainingDisplayed() {
        return isDisplayed(id(REWARD_REMAINING));
    }

    public boolean isUserPointsDisplayed() {
        return isDisplayed(id(USER_POINTS));
    }

    public boolean isWarningBoxDisplayed() {
        return isDisplayed(id(WARNING_BOX));
    }

    public String getWarningMessage() {
        return getText(id(WARNING_MESSAGE));
    }

    public boolean isWarningAboutInsufficientPoints() {
        return isTextDisplayed("Not enough points");
    }
}