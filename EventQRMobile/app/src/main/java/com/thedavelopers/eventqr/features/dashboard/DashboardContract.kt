package com.thedavelopers.eventqr.features.dashboard

import com.thedavelopers.eventqr.features.dashboard.model.dto.DashboardSummary
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse

interface DashboardContract {
    interface View {
        fun showLoading(isLoading: Boolean)
        fun showSummary(summary: DashboardSummary)
        fun showTransactionHistoryLoading(isLoading: Boolean)
        fun showTransactionHistory(items: List<TransactionResponse>)
        fun showTransactionHistoryError(message: String)
        fun showError(message: String)
        fun showMessage(message: String)
        fun openSection(title: String, message: String)
        fun updateHeader(role: String?, name: String?)
    }
}