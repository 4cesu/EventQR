package com.thedavelopers.eventqr.features.registrations

import java.util.Locale

/**
 * Formats event_registrations.registration_number (per-event sequence assigned by DB trigger,
 * migration V13) for display and print. Zero-padded to 3 digits ("#012") so printed cards keep
 * a consistent column width; numbers of 1000+ simply render wider.
 */
object RegistrationNumberFormatter {

    /** "#012" style; returns null when absent so callers can fall back to legacy display. */
    fun format(registrationNumber: Int?): String? =
        registrationNumber?.let { String.format(Locale.US, "#%03d", it) }
}
