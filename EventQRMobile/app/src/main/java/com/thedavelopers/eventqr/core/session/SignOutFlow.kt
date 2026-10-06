package com.thedavelopers.eventqr.core.session

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.thedavelopers.eventqr.features.auth.login.LoginActivity
import kotlinx.coroutines.launch

/**
 * Sign-out shared by every portal: confirm, revoke the token on the server, clear the local
 * session, and return to the login screen with a fresh back stack.
 */
object SignOutFlow {

    fun confirmAndSignOut(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { dialog, _ ->
                dialog.dismiss()
                signOut(activity)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun signOut(activity: AppCompatActivity) {
        activity.lifecycleScope.launch {
            SessionLogout.signOut(activity)
            activity.startActivity(
                Intent(activity, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            activity.finish()
        }
    }
}
