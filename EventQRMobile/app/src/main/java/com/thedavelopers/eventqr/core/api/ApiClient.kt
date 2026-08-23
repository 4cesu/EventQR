package com.thedavelopers.eventqr.core.api

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.gson.GsonBuilder
import com.thedavelopers.eventqr.core.session.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant

object ApiClient {
    @Volatile
    private var apiService: ApiService? = null

    fun getService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: buildService(context.applicationContext).also { apiService = it }
        }
    }

    private fun buildService(context: Context): ApiService {
        val sessionManager = SessionManager(context)
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeAdapter)
            .setLenient()
            .create()

        val isDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
        if (isDebuggable) {
            val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            clientBuilder.addInterceptor(loggingInterceptor)
        }
        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
