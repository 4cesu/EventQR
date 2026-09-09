package com.thedavelopers.eventqr.pages;

import com.thedavelopers.eventqr.base.BaseTest;

/**
 * Edit Profile screen (TestFlow: EP-1..EP-5).
 * Layout: activity_edit_profile.xml
 */
public class EditProfilePage extends BaseTest {

    private static final String TOOLBAR = "toolbarEditProfile";
    private static final String SWIPE_REFRESH = "swipeRefreshEditProfile";

    // Form
    private static final String TIL_FULL_NAME = "tilFullName";
    private static final String FULL_NAME_FIELD = "edtFullName";
    private static final String TIL_EMAIL = "tilEmail";
    private static final String EMAIL_FIELD = "edtEmail";
    private static final String TIL_PHONE = "tilPhone";
    private static final String PHONE_FIELD = "edtPhone";

    // Actions
    private static final String SAVE_BUTTON = "btnSaveChanges";
    private static final String CHANGE_PASSWORD_BUTTON = "btnChangePassword";

    // Error state
    private static final String ERROR_CARD = "cardError";
    private static final String API_ERROR_TEXT = "txtApiError";
    private static final String RETRY_BUTTON = "btnRetryProfileLoad";

    public boolean isEditProfileVisible() {
        return isDisplayed(id(FULL_NAME_FIELD));
    }

    public void tapBack() {
        tap(id(TOOLBAR));
    }

    public boolean isFullNameDisplayed() {
        return isDisplayed(id(TIL_FULL_NAME));
    }

    public String getFullName() {
        return getText(id(FULL_NAME_FIELD));
    }

    public boolean isEmailDisplayed() {
        return isDisplayed(id(TIL_EMAIL));
    }

    public String getEmail() {
        return getText(id(EMAIL_FIELD));
    }

    public boolean isPhoneDisplayed() {
        return isDisplayed(id(TIL_PHONE));
    }

    public void clearPhone() {
        type(id(PHONE_FIELD), "");
    }

    public void enterPhone(String phone) {
        type(id(PHONE_FIELD), phone);
    }

    public String getPhone() {
        return getText(id(PHONE_FIELD));
    }

    public boolean isSaveButtonDisplayed() {
        return isDisplayed(id(SAVE_BUTTON));
    }

    public void tapSave() {
        tap(id(SAVE_BUTTON));
    }

    public boolean isChangePasswordButtonDisplayed() {
        return isDisplayed(id(CHANGE_PASSWORD_BUTTON));
    }

    public void tapChangePassword() {
        tap(id(CHANGE_PASSWORD_BUTTON));
    }

    public boolean isErrorCardDisplayed() {
        return isDisplayed(id(ERROR_CARD));
    }

    public String getApiErrorText() {
        return getText(id(API_ERROR_TEXT));
    }

    public void tapRetry() {
        tap(id(RETRY_BUTTON));
    }

    public void refresh() {
        swipeDown();
    }
}