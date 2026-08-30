package com.thedavelopers.eventqr.features.registrations

import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse
import java.time.Duration
import java.time.Instant

object RegistrationsCache {

    private val freshness = Duration.ofSeconds(30)

    @Volatile
    private var registrations: List<RegistrationResponse>? = null

    @Volatile
    private var fetchedAt: Instant? = null

    fun get(): List<RegistrationResponse>? = registrations

    fun isFresh(now: Instant = Instant.now()): Boolean {
        val fetched = fetchedAt ?: return false
        return !now.isAfter(fetched.plus(freshness))
    }

    @Synchronized
    fun set(items: List<RegistrationResponse>) {
        registrations = items
        fetchedAt = Instant.now()
    }

    @Synchronized
    fun addRegistration(registration: RegistrationResponse) {
        registrations = (registrations ?: emptyList())
            .filter { it.registrationId != registration.registrationId } + registration
        fetchedAt = Instant.now()
    }

    fun clear() {
        synchronized(this) {
            registrations = null
            fetchedAt = null
        }
    }
}