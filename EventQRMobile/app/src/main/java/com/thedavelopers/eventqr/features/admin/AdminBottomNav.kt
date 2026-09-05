package com.thedavelopers.eventqr.features.admin

import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.admin.dashboard.AdminDashboardActivity
import com.thedavelopers.eventqr.features.admin.logs.AdminAuditLogsActivity
import com.thedavelopers.eventqr.features.admin.users.AdminAccountManagementActivity
import com.thedavelopers.eventqr.features.common.bindBottomNavItem

enum class AdminBottomNavItem {
    DASHBOARD,
    REQUESTS,
    ACCOUNTS,
    LOGS,
}

fun AppCompatActivity.configureAdminBottomNav(selectedItem: AdminBottomNavItem?) {
    bindBottomNavItem(
        R.id.navDashboard,
        selectedItem == AdminBottomNavItem.DASHBOARD,
        R.drawable.ic_nav_home,
        "Dashboard",
        AdminDashboardActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navRequests,
        selectedItem == AdminBottomNavItem.REQUESTS,
        R.drawable.ic_action_request,
        "Requests",
        AdminEventApprovalBackendActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navAccounts,
        selectedItem == AdminBottomNavItem.ACCOUNTS,
        R.drawable.ic_group,
        "Accounts",
        AdminAccountManagementActivity::class.java,
    )
    bindBottomNavItem(
        R.id.navLogs,
        selectedItem == AdminBottomNavItem.LOGS,
        R.drawable.ic_file,
        "Logs",
        AdminAuditLogsActivity::class.java,
    )
}