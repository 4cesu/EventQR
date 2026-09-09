package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Manage Scan Purposes screen (TestFlow: MSP-1..MSP-8).
 * Resource IDs are set programmatically in ManageScanPurposesActivity.kt
 * and declared in app/src/main/res/values/ids.xml.
 */
public class ManageScanPurposesPage extends BaseTest {

    // Purpose list container
    private static final String PURPOSE_HOST = "msp_purpose_host";

    // Top-right "+ Add" button (shared nav header action)
    private static final String ADD_BUTTON = "nav_header_action";

    // Purpose card toggle (first visible match)
    private static final String ITEM_TOGGLE = "msp_purpose_toggle";

    // Add/edit dialog fields
    private static final String TYPE_SPINNER = "msp_type_spinner";
    private static final String NAME_FIELD = "msp_name_input";
    private static final String DESC_FIELD = "msp_desc_input";
    private static final String POINTS_FIELD = "msp_points_input";
    private static final String DUPLICATE_CHECK = "msp_duplicate_check";
    private static final String TRACKING_ONLY_CHECK = "msp_tracking_only_check";

    public boolean isScanPurposesVisible() {
        return isDisplayed(id(PURPOSE_HOST));
    }

    public boolean isAddButtonDisplayed() {
        return isDisplayed(id(ADD_BUTTON));
    }

    public void tapAddPurpose() {
        tap(id(ADD_BUTTON));
    }

    public boolean isPurposeItemDisplayed() {
        return isDisplayed(id(ITEM_TOGGLE));
    }

    public boolean isItemToggleDisplayed() {
        return isDisplayed(id(ITEM_TOGGLE));
    }

    public void enterName(String name) {
        type(id(NAME_FIELD), name);
    }

    public void enterDescription(String description) {
        type(id(DESC_FIELD), description);
    }

    public void enterDefaultPoints(String points) {
        type(id(POINTS_FIELD), points);
    }

    public void selectType(String typeLabel) {
        tap(id(TYPE_SPINNER));
        tapByText(typeLabel);
    }

    public void setDuplicateAllowed(boolean allowed) {
        if (allowed != isDisplayed(id(DUPLICATE_CHECK))) {
            tap(id(DUPLICATE_CHECK));
        }
    }

    public void setTrackingOnly(boolean trackingOnly) {
        if (trackingOnly != isDisplayed(id(TRACKING_ONLY_CHECK))) {
            tap(id(TRACKING_ONLY_CHECK));
        }
    }

    public void tapSave() {
        tapByText("Save");
    }

    public void addPurpose(String name, String typeLabel, String points, boolean trackingOnly) {
        tapAddPurpose();
        selectType(typeLabel);
        enterName(name);
        enterDefaultPoints(points);
        setTrackingOnly(trackingOnly);
        tapSave();
    }

    public boolean isPurposeVisible(String name) {
        return isTextDisplayed(name);
    }

    public void tapPurpose(String name) {
        tapByText(name);
    }

    public void tapDelete() {
        tapByText("Delete");
    }

    public void confirmDelete() {
        tapByText("Delete");
    }
}
