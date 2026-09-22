package com.thedavelopers.eventqr.features.staff

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.AccountRole
import com.thedavelopers.eventqr.core.session.SessionManager
import com.thedavelopers.eventqr.core.util.RoleMapper
import com.thedavelopers.eventqr.features.attendee.AttendeeEditProfileActivity
import com.thedavelopers.eventqr.features.attendee.AttendeeRepository
import com.thedavelopers.eventqr.features.registrations.RegistrationsCache
import com.thedavelopers.eventqr.features.users.model.dto.UserResponse
import kotlinx.coroutines.launch

open class StaffProfileActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: AttendeeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        repository = AttendeeRepository(this)

        if (RoleMapper.normalizeRole(sessionManager.getUserRole()) != AccountRole.STAFF.name) {
            Toast.makeText(this, "Access Denied: Staff only", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_staff_profile)

        setupStaffBottomNav()

        // EventQR - UI safeguard beyond SRS/SDD explicit spec (no confirm-dialog requirement stated for sign out)
        findViewById<View>(R.id.cardSignOut).setOnClickListener {
            showSignOutConfirmation()
        }

        val launchEditProfile = {
            startActivity(Intent(this, AttendeeEditProfileActivity::class.java))
        }
        findViewById<View>(R.id.cardEditProfile)?.setOnClickListener { launchEditProfile() }
        findViewById<View>(R.id.btnEditProfile)?.setOnClickListener { launchEditProfile() }

        // Hide attendee-only rows and their dividers
        findViewById<View>(R.id.cardTransactionHistory)?.visibility = View.GONE
        findViewById<View>(R.id.dividerTransactionHistory)?.visibility = View.GONE
        findViewById<View>(R.id.cardClaimedRewards)?.visibility = View.GONE
        findViewById<View>(R.id.dividerClaimedRewards)?.visibility = View.GONE
        findViewById<View>(R.id.cardMyEventRequests)?.visibility = View.GONE
        findViewById<View>(R.id.dividerMyEventRequests)?.visibility = View.GONE

        findViewById<SwipeRefreshLayout>(R.id.swipeRefreshProfile)?.let { swipe ->
            swipe.setColorSchemeResources(R.color.accent_signal)
            swipe.setOnRefreshListener { loadProfile() }
        }
    }

    private fun showSignOutConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { dialog, _ ->
                dialog.dismiss()
                performSignOut()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performSignOut() {
        RegistrationsCache.clear()
        sessionManager.clearSession()
        startActivity(
            Intent(this, com.thedavelopers.eventqr.features.auth.login.LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        renderProfile()

        lifecycleScope.launch {
            when (val result = repository.getMyProfile()) {
                is NetworkResult.Success -> {
                    val user = result.data
                    sessionManager.updateProfile(user.fullName, user.phoneNumber, user.email)
                    sessionManager.saveRole(user.role)
                    renderProfile(user)
                }
                else -> Unit
            }
            findViewById<SwipeRefreshLayout>(R.id.swipeRefreshProfile)?.isRefreshing = false
        }
    }

    private fun renderProfile(user: UserResponse? = null) {
        val name = user?.fullName ?: sessionManager.getFullName() ?: "Staff User"
        findViewById<TextView>(R.id.txtProfileName)?.text = name
        findViewById<TextView>(R.id.txtProfileRole)?.text = (user?.role?.name ?: sessionManager.getUserRole())
            ?.takeIf { it.isNotBlank() }
            ?.let { RoleMapper.getDisplayName(it) }
            ?: "STAFF"

        // Initial avatar
        findViewById<TextView>(R.id.txtProfileInitial)?.text =
            name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        // Profile detail rows
        findViewById<TextView>(R.id.txtProfileDetailName)?.text = user?.fullName ?: sessionManager.getFullName().orEmpty()
        findViewById<TextView>(R.id.txtProfileDetailEmail)?.text = user?.email ?: sessionManager.getEmail().orEmpty()
        findViewById<TextView>(R.id.txtProfileDetailPhone)?.text =
            (user?.phoneNumber ?: sessionManager.getPhone())?.takeIf { it.isNotBlank() } ?: "\u2014"
    }

    private fun setupStaffBottomNav() {
        configureStaffProfileBottomNav()
    }
}
