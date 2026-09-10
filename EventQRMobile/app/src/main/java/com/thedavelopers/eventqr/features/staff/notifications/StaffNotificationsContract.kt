package com.thedavelopers.eventqr.features.staff

import com.thedavelopers.eventqr.features.notifications.model.dto.NotificationResponse

interface StaffNotificationsContract {
    interface View {
        fun showLoading(isLoading: Boolean)
        fun showMessage(message: String)
        fun showContent()
        fun showError(message: String)
        fun renderNotifications(items: List<NotificationResponse>)
        fun setMarkAllEnabled(enabled: Boolean)
    }
}