package com.thedavelopers.eventqr.core.navigation

import android.content.Context
import android.content.Intent
import com.thedavelopers.eventqr.features.auth.changepassword.ChangePasswordActivity

object AppNavigator {
    fun changePassword(context: Context): Intent = Intent(context, ChangePasswordActivity::class.java)
}

