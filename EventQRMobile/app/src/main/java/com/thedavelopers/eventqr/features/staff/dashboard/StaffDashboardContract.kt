package com.thedavelopers.eventqr.features.staff

import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse

interface StaffDashboardContract {
    interface View {
        fun renderRecentScans(items: List<TransactionResponse>)
        fun updateStats(scans: Int, checkins: Int)
        fun showMessage(message: String)
        fun showLoading(isLoading: Boolean)
        fun showNotificationBadge(hasUnread: Boolean)
    }
}
