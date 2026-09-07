package com.thedavelopers.eventqr.features.staff

import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.NotificationStatus
import com.thedavelopers.eventqr.core.api.dto.TransactionResult
import kotlinx.coroutines.Job
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
            when (val result = repository.getEvents()) {
                is NetworkResult.Success -> {
                    when (val todayResult = repository.getMyTodayTransactions()) {
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
                }
                is NetworkResult.Error -> view?.showMessage(result.message)
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
