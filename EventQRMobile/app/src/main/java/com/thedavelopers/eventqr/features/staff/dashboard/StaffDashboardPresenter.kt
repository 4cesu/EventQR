package com.thedavelopers.eventqr.features.staff

import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.NotificationStatus
import com.thedavelopers.eventqr.core.api.dto.TransactionResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class StaffDashboardPresenter(
    private var view: StaffDashboardContract.View?,
    private val repository: StaffRepository,
) {
    private var job: Job? = null

    fun detach() {
        job?.cancel()
        view = null
    }

    fun loadData() {
        view?.showLoading(true)
        job = kotlinx.coroutines.MainScope().launch {
            val (eventsResult, todayResult) = coroutineScope {
                val eventsDeferred = async { repository.getEvents() }
                val todayDeferred = async { repository.getMyTodayTransactions() }
                eventsDeferred.await() to todayDeferred.await()
            }

            when (eventsResult) {
                is NetworkResult.Error -> view?.showMessage(eventsResult.message)
                is NetworkResult.Success -> Unit
                NetworkResult.Loading -> Unit
            }

            when (todayResult) {
                is NetworkResult.Success -> {
                    val sortedTransactions = todayResult.data
                        .sortedByDescending { it.scannedAt ?: java.time.Instant.EPOCH }
                    view?.renderRecentScans(sortedTransactions.take(5))
                    view?.updateStats(
                        sortedTransactions.size,
                        sortedTransactions.count {
                            (it.transactionType.name == "ENTRY" || it.transactionType.name == "ATTENDANCE") &&
                                it.transactionResult == TransactionResult.APPROVED
                        },
                    )
                }
                is NetworkResult.Error -> {
                    view?.renderRecentScans(emptyList())
                    view?.updateStats(
                        0,
                        0,
                    )
                }
                NetworkResult.Loading -> Unit
            }

            when (val notifResult = repository.getMyNotifications()) {
                is NetworkResult.Success -> {
                    val unreadCount = notifResult.data.count { it.status != NotificationStatus.READ && it.readAt == null }
                    view?.showNotificationBadge(unreadCount > 0)
                }
                is NetworkResult.Error -> view?.showNotificationBadge(false)
                NetworkResult.Loading -> Unit
            }
            view?.showLoading(false)
        }
    }
}
