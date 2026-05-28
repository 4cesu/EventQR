package com.thedavelopers.eventqr.features.attendee

import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse

interface TransactionHistoryContract {
    interface View : AttendeeView {
        override fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun renderTransactions(items: List<TransactionResponse>)
    }
}
