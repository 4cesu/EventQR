package com.thedavelopers.eventqr.features.staff

import android.content.Context
import com.thedavelopers.eventqr.core.api.ApiClient
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.ScanPurposeCode
import com.thedavelopers.eventqr.core.api.safeApiCall
import com.thedavelopers.eventqr.features.idprinting.model.dto.IdBatchPrintRequest
import com.thedavelopers.eventqr.features.notifications.model.dto.NotificationResponse
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionGrantRequest
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionScanRequest
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse
import com.thedavelopers.eventqr.features.scanpurposes.model.dto.ScanPurposeRequest
import com.thedavelopers.eventqr.features.scanpurposes.model.dto.ScanPurposeResponse
import com.thedavelopers.eventqr.features.staff.model.dto.StaffAssignedEventResponse
import com.thedavelopers.eventqr.features.staff.model.dto.ScanVerificationResponse
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionRequest
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse

class StaffRepository(context: Context) {
    private val apiService = ApiClient.getService(context)

    suspend fun getEvents(): NetworkResult<List<StaffAssignedEventResponse>> = safeApiCall { apiService.getStaffEvents() }

    suspend fun getEventById(eventId: String) = safeApiCall { apiService.getStaffEventById(eventId) }

    suspend fun getScanPurposesByEvent(eventId: String) = safeApiCall { apiService.getStaffScanPurposes(eventId) }

    suspend fun verifyScan(request: TransactionRequest): NetworkResult<ScanVerificationResponse> = safeApiCall {
        apiService.verifyScan(request.eventId.toString(), request)
    }

    suspend fun createTransaction(request: TransactionRequest, purposeCode: ScanPurposeCode) = safeApiCall {
        when (purposeCode) {
            ScanPurposeCode.ENTRY -> apiService.logEntry(request.eventId.toString(), request)
            ScanPurposeCode.ATTENDANCE -> apiService.logAttendance(request.eventId.toString(), request)
            ScanPurposeCode.BENEFIT_CLAIM -> apiService.logBenefitClaim(request.eventId.toString(), request)
            ScanPurposeCode.BOOTH_VISIT, ScanPurposeCode.SESSION_VISIT -> apiService.logBoothVisit(request.eventId.toString(), request)
            ScanPurposeCode.REWARD_REDEMPTION_SCAN, ScanPurposeCode.REWARD_REDEMPTION -> apiService.logRewardRedemption(request.eventId.toString(), request)
            ScanPurposeCode.EXIT -> apiService.logExit(request.eventId.toString(), request)
            else -> apiService.createTransaction(request)
        }
    }

    suspend fun getTransactionsByEvent(eventId: String) = safeApiCall { apiService.getStaffTransactions(eventId) }

    suspend fun getMyTransactions(eventId: String? = null, purposeId: String? = null) = safeApiCall {
        apiService.getStaffMyTransactions(eventId, purposeId)
    }

    suspend fun getTodayTransactionsByEvent(eventId: String) = safeApiCall { apiService.getStaffTodayTransactions(eventId) }

    suspend fun getAttendeeTransactions(eventId: String, attendeeId: String) = safeApiCall { apiService.getStaffAttendeeTransactions(eventId, attendeeId) }

    suspend fun getAttendeeByEvent(eventId: String, attendeeId: String) = safeApiCall {
        apiService.getStaffAttendee(eventId, attendeeId)
    }

    suspend fun getRewardBalance(eventId: String, attendeeUserId: String) = safeApiCall {
        apiService.getRewardBalance(eventId, attendeeUserId)
    }

    suspend fun getRewardsByEvent(eventId: String) = safeApiCall {
        apiService.getRewardsByEvent(eventId)
    }

    suspend fun rewardRedemptionScan(request: RewardRedemptionScanRequest) = safeApiCall {
        apiService.rewardRedemptionScan(request)
    }

    suspend fun redeemRewardStaff(request: RewardRedemptionGrantRequest) = safeApiCall {
        apiService.redeemRewardStaff(request)
    }

    suspend fun getLatestScan(eventId: String) = safeApiCall { apiService.getLatestScan(eventId) }

    suspend fun printAttendeeId(eventId: String, attendeeId: String) = safeApiCall { apiService.printAttendeeId(eventId, attendeeId) }

    suspend fun reprintAttendeeId(eventId: String, attendeeId: String) = safeApiCall { apiService.reprintAttendeeId(eventId, attendeeId) }

    suspend fun getStaffPrintLogs(eventId: String) = safeApiCall { apiService.getStaffPrintLogs(eventId) }

    suspend fun printIdBatch(eventId: String, attendeeUserIds: List<java.util.UUID>, reprint: Boolean) =
        safeApiCall { apiService.printIdBatch(eventId, IdBatchPrintRequest(attendeeUserIds, reprint)) }

    suspend fun getRegistrationsByEvent(eventId: String) = safeApiCall { apiService.getRegistrationsByEvent(eventId) }

    suspend fun getNotificationsByRecipient(recipientUserId: String) = safeApiCall { apiService.getNotificationsByRecipient(recipientUserId) }

    suspend fun getMyNotifications(): NetworkResult<List<NotificationResponse>> = safeApiCall { apiService.getMyNotifications() }
}
