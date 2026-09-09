package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Manage Rewards screen (TestFlow: MR-1..MR-8).
 * Resource IDs are set programmatically in ManageRewardsActivity.kt
 * and declared in app/src/main/res/values/ids.xml.
 */
public class ManageRewardsPage extends BaseTest {

    // Event selector + summary
    private static final String EVENT_SELECTOR = "mrw_event_selector";
    private static final String EVENT_SUMMARY_TITLE = "mrw_event_summary_title";
    private static final String EVENT_SUMMARY_COUNT = "mrw_event_summary_count";

    // Rewards enabled toggle
    private static final String REWARDS_ENABLED_SWITCH = "mrw_rewards_switch";

    // Section
    private static final String SECTION_TITLE = "mrw_section_title";

    // Reward host (list container)
    private static final String REWARD_HOST = "mrw_reward_host";

    // Reward card items (first visible match)
    private static final String ITEM_NAME = "mrw_reward_name";
    private static final String ITEM_POINTS = "mrw_reward_points";
    private static final String ITEM_STOCK = "mrw_reward_stock";
    private static final String ITEM_EDIT_BUTTON = "mrw_reward_edit";
    private static final String ITEM_DELETE_BUTTON = "mrw_reward_remove";

    // Top-right "+ Add" button (shared nav header action)
    private static final String ADD_BUTTON = "nav_header_action";

    // Create / edit reward dialog
    private static final String NAME_FIELD = "mrw_reward_title_input";
    private static final String POINTS_FIELD = "mrw_reward_points_input";
    private static final String STOCK_FIELD = "mrw_reward_quantity_input";
    private static final String DUPLICATE_SWITCH = "mrw_reward_duplicate_switch";

    public boolean isManageRewardsVisible() {
        return isDisplayed(id(REWARDS_ENABLED_SWITCH)) || isDisplayed(id(REWARD_HOST));
    }

    public boolean isEventSummaryDisplayed() {
        return isDisplayed(id(EVENT_SUMMARY_TITLE)) || isDisplayed(id(EVENT_SUMMARY_COUNT));
    }

    public String getEventSummaryTitle() {
        return getText(id(EVENT_SUMMARY_TITLE));
    }

    public void pickEvent(String eventTitle) {
        tap(id(EVENT_SELECTOR));
        tapByText(eventTitle);
    }

    public boolean isRewardsEnabledSwitchDisplayed() {
        return isDisplayed(id(REWARDS_ENABLED_SWITCH));
    }

    public void setRewardsEnabled(boolean enabled) {
        if (enabled != isDisplayed(id(REWARDS_ENABLED_SWITCH))) {
            tap(id(REWARDS_ENABLED_SWITCH));
        }
    }

    public boolean isAddButtonDisplayed() {
        return isDisplayed(id(ADD_BUTTON));
    }

    public void tapAddReward() {
        tap(id(ADD_BUTTON));
    }

    public boolean isRewardItemDisplayed() {
        return isDisplayed(id(ITEM_NAME));
    }

    public String getItemName() {
        return getText(id(ITEM_NAME));
    }

    public String getItemPoints() {
        return getText(id(ITEM_POINTS));
    }

    public String getItemStock() {
        return getText(id(ITEM_STOCK));
    }

    public boolean isRewardVisible(String name) {
        return isTextDisplayed(name);
    }

    public void tapReward(String name) {
        tapByText(name);
    }

    public void tapEditReward(String name) {
        tapByText(name);
        tap(id(ITEM_EDIT_BUTTON));
    }

    public void tapDeleteReward(String name) {
        tapByText(name);
        tap(id(ITEM_DELETE_BUTTON));
    }

    public void confirmDelete() {
        tapByText("Remove");
    }

    // Reward dialog helpers
    public void enterName(String name) {
        type(id(NAME_FIELD), name);
    }

    public void enterPoints(String points) {
        type(id(POINTS_FIELD), points);
    }

    public void enterStock(String stock) {
        type(id(STOCK_FIELD), stock);
    }

    public void setDuplicateClaimsAllowed(boolean allowed) {
        if (allowed != isDisplayed(id(DUPLICATE_SWITCH))) {
            tap(id(DUPLICATE_SWITCH));
        }
    }

    public void tapSave() {
        tapByText("Save");
    }

    public void addReward(String name, String points, String stock, boolean duplicatesAllowed) {
        tapAddReward();
        enterName(name);
        enterPoints(points);
        enterStock(stock);
        setDuplicateClaimsAllowed(duplicatesAllowed);
        tapSave();
    }
}
