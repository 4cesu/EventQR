package com.thedavelopers.eventqr

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.thedavelopers.eventqr.core.session.SessionEvents
import com.thedavelopers.eventqr.features.registrations.RegistrationsCache
import com.thedavelopers.eventqr.features.auth.login.LoginActivity

class EventQrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionEvents.onSessionExpired = {
            // Called from an OkHttp thread once the server has refused the refresh token.
            Handler(Looper.getMainLooper()).post {
                RegistrationsCache.clear()
                Toast.makeText(this, "Your session has expired. Please sign in again.", Toast.LENGTH_LONG).show()
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        }
    }
}
