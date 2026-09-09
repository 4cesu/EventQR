package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Search User Account screen (TestFlow: SU-1..SU-6).
 * Layouts: activity_search_user_account.xml, item_search_user_account.xml
 */
public class SearchUserPage extends BaseTest {

    private static final String BACK_BUTTON = "btnSearchUserBack";
    private static final String SEARCH_INPUT_LAYOUT = "layoutSearchInput";
    private static final String SEARCH_FIELD = "edtSearchUser";

    private static final String RECYCLER = "recyclerSearchUsers";
    private static final String LOADING = "progressSearchUsers";
    private static final String EMPTY_MESSAGE = "txtSearchEmpty";

    private static final String ASSIGN_BUTTON = "btnAssignStaff";

    // Item elements
    private static final String ITEM_CARD = "cardSearchUser";
    private static final String ITEM_AVATAR_INITIAL = "txtUserAvatarInitial";
    private static final String ITEM_NAME = "txtSearchUserName";
    private static final String ITEM_EMAIL = "txtSearchUserEmail";
    private static final String ITEM_SELECTED_CHECK = "imgSelectedCheck";

    public boolean isSearchUserVisible() {
        return isDisplayed(id(SEARCH_FIELD)) && isDisplayed(id(ASSIGN_BUTTON));
    }

    public void tapBack() {
        tap(id(BACK_BUTTON));
    }

    public void search(String query) {
        type(id(SEARCH_FIELD), query);
    }

    public void clearSearch() {
        type(id(SEARCH_FIELD), "");
    }

    public boolean isLoadingDisplayed() {
        return isDisplayed(id(LOADING));
    }

    public boolean isEmptyMessageDisplayed() {
        return isDisplayed(id(EMPTY_MESSAGE));
    }

    public boolean isUserItemDisplayed() {
        return isDisplayed(id(ITEM_NAME));
    }

    /**
     * Selects a matching user card by full name.
     */
    public void selectUser(String name) {
        if (isDisplayed(id(ITEM_NAME))) {
            String current = getItemName();
            if (current.equals(name) || current.contains(name)) {
                tap(id(ITEM_NAME));
                return;
            }
        }
        tapByText(name);
    }

    public String getItemName() {
        return getText(id(ITEM_NAME));
    }

    public String getItemEmail() {
        return getText(id(ITEM_EMAIL));
    }

    public boolean isSelectedCheckDisplayed() {
        return isDisplayed(id(ITEM_SELECTED_CHECK));
    }

    public boolean isAssignButtonEnabled() {
        return isDisplayed(id(ASSIGN_BUTTON));
    }

    public void tapAssign() {
        tap(id(ASSIGN_BUTTON));
    }

    /**
     * Full search-and-assign flow.
     */
    public void searchAndSelect(String query, String name) {
        search(query);
        selectUser(name);
    }
}