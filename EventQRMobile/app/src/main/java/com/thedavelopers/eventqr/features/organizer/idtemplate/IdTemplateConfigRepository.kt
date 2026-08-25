package com.thedavelopers.eventqr.features.organizer.idtemplate

import android.content.Context
import com.thedavelopers.eventqr.core.api.ApiClient
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.safeApiCall
import com.thedavelopers.eventqr.features.idprinting.model.dto.IdTemplateConfigRequest
import com.thedavelopers.eventqr.features.idprinting.model.dto.IdTemplateConfigResponse

class IdTemplateConfigRepository(context: Context) {
    private val apiService = ApiClient.getService(context)

    suspend fun fetchConfig(eventId: String): NetworkResult<IdTemplateConfigResponse> =
        safeApiCall { apiService.getIdTemplateConfig(eventId) }

    suspend fun saveConfig(
        eventId: String,
        visibleFields: List<String>,
    ): NetworkResult<IdTemplateConfigResponse> =
        safeApiCall { apiService.saveIdTemplateConfig(eventId, IdTemplateConfigRequest(visibleFields)) }
}
