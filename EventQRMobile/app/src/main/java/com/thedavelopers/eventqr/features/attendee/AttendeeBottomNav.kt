package com.thedavelopers.eventqr.features.attendee

import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.common.bindBottomNavItem
import com.thedavelopers.eventqr.features.dashboard.DashboardActivity

enum class AttendeeBottomNavItem {
    DASHBOARD,
    EVENTS,
    REGISTERED,
    REWARDS,
    PROFILE,
}

fun AppCompatActivity.configureAttendeeBottomNav(selectedItem: AttendeeBottomNavItem) {
    bindBottomNavItem(
        R.id.navDashboard,
        selectedItem == AttendeeBottomNavItem.DASHBOARD,
        R.drawable.ic_home,
        "Home",
        DashboardActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navEvents,
        selectedItem == AttendeeBottomNavItem.EVENTS,
        R.drawable.ic_calendar,
        "Events",
        AttendeeEventsActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navRegistered,
        selectedItem == AttendeeBottomNavItem.REGISTERED,
        R.drawable.ic_nav_registered,
        "Registered",
        RegisteredEventsActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navRewards,
        selectedItem == AttendeeBottomNavItem.REWARDS,
        R.drawable.ic_nav_gift,
        "Rewards",
        AttendeeRewardsActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navProfile,
        selectedItem == AttendeeBottomNavItem.PROFILE,
        R.drawable.ic_nav_profile,
        "Profile",
        AttendeeProfileActivity::class.java,
    )
}