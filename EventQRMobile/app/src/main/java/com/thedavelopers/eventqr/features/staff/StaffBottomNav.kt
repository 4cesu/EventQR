package com.thedavelopers.eventqr.features.staff

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.common.bindBottomNavItem
import com.thedavelopers.eventqr.features.staff.scanner.ScannerActivity

enum class StaffBottomNavItem {
    DASHBOARD,
    SCANNER,
    EVENTS,
    LOGS,
}

fun AppCompatActivity.configureStaffBottomNav(selectedItem: StaffBottomNavItem, currentEventId: String? = null) {
    bindBottomNavItem(
        R.id.navDashboard,
        selectedItem == StaffBottomNavItem.DASHBOARD,
        R.drawable.ic_nav_home,
        "Dashboard",
        StaffDashboardActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navScanner,
        selectedItem == StaffBottomNavItem.SCANNER,
        R.drawable.ic_scan,
        "Scan",
        ScannerActivity::class.java,
    ) {
        currentEventId?.takeIf { it.isNotBlank() }?.let { putExtra(StaffScreenExtras.EXTRA_EVENT_ID, it) }
    }
    bindBottomNavItem(
        R.id.navEvents,
        selectedItem == StaffBottomNavItem.EVENTS,
        R.drawable.ic_calendar,
        "Events",
        StaffAssignedEventsActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navLogs,
        selectedItem == StaffBottomNavItem.LOGS,
        R.drawable.ic_file,
        "Logs",
        StaffTransactionsActivity::class.java,
    ) {
        currentEventId?.takeIf { it.isNotBlank() }?.let { putExtra(StaffScreenExtras.EXTRA_EVENT_ID, it) }
    }
}

fun AppCompatActivity.configureStaffProfileBottomNav() {
    findViewById<View>(R.id.navRegistered)?.visibility = View.GONE

    bindBottomNavItem(
        R.id.navDashboard,
        false,
        R.drawable.ic_nav_home,
        "Dashboard",
        StaffDashboardActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navEvents,
        false,
        R.drawable.ic_scan,
        "Scan QR",
        ScannerActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navRewards,
        false,
        R.drawable.ic_file,
        "Logs",
        StaffTransactionsActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navProfile,
        true,
        R.drawable.ic_nav_profile,
        "Profile",
        StaffProfileActivity::class.java,
    )
}