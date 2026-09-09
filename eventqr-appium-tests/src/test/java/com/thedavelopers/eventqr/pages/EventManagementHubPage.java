package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Event Management Hub screen (TestFlow: EMH-1..EMH-8).
 * Resource IDs are set programmatically in EventManagementHubActivity.kt
 * and declared in app/src/main/res/values/ids.xml.
 */
public class EventManagementHubPage extends BaseTest {

    // Header / banner
    private static final String HEADER_BANNER = "emh_header_banner";
    private static final String HUB_STATUS = "emh_status_badge";
    private static final String HUB_TITLE = "emh_event_title";

    // Stats
    private static final String STATS_ROW = "emh_stats_row";
    private static final String STAT_REGISTERED = "emh_stat_registered";
    private static final String STAT_CAPACITY = "emh_stat_capacity";
    private static final String STAT_AVAILABLE = "emh_stat_available";

    // Section title
    private static final String SECTION_TITLE = "emh_section_title";

    // Menu cards
    private static final String MENU_EDIT = "emh_menu_edit";
    private static final String MENU_STAFF = "emh_menu_staff";
    private static final String MENU_SCAN = "emh_menu_scan";
    private static final String MENU_TRANSACTION = "emh_menu_transaction";
    private static final String MENU_ID = "emh_menu_id";

    public boolean isEventHubVisible() {
        return isDisplayed(id(STATS_ROW)) || isDisplayed(id(STAT_REGISTERED))
                || isDisplayed(id(HEADER_BANNER));
    }

    public boolean isTitleDisplayed() {
        return isDisplayed(id(HUB_TITLE));
    }

    public String getTitle() {
        return getText(id(HUB_TITLE));
    }

    public boolean isStatusDisplayed() {
        return isDisplayed(id(HUB_STATUS));
    }

    public boolean isStatsRowDisplayed() {
        return isDisplayed(id(STATS_ROW));
    }

    public boolean isRegisteredStatVisible() {
        return isDisplayed(id(STAT_REGISTERED));
    }

    public boolean isCapacityStatVisible() {
        return isDisplayed(id(STAT_CAPACITY));
    }

    public boolean isAvailableStatVisible() {
        return isDisplayed(id(STAT_AVAILABLE));
    }

    public void tapEditEventDetails() {
        tap(id(MENU_EDIT));
    }

    public void tapStaffAssignment() {
        tap(id(MENU_STAFF));
    }

    public void tapScanPurposes() {
        tap(id(MENU_SCAN));
    }

    public void tapTransactionRules() {
        tap(id(MENU_TRANSACTION));
    }

    public void tapIdTemplate() {
        tap(id(MENU_ID));
    }

    public boolean isMenuOptionVisible(String label) {
        return isTextDisplayed(label);
    }
}
