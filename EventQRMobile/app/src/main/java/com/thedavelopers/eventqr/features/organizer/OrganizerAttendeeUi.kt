package com.thedavelopers.eventqr.features.organizer

import android.content.Context
import com.thedavelopers.eventqr.R
import java.util.Locale

internal fun OrganizerMvpAttendee.statusBucket(): String {
    val current = currentEventStatus.trim()
    val registration = registrationStatus.trim()
    return when {
        current.contains("checked in", ignoreCase = true) ||
            current.contains("entered", ignoreCase = true) ||
            current.contains("attended", ignoreCase = true) -> "Checked In"
        current.contains("exited", ignoreCase = true) || registration.equals("Exited", ignoreCase = true) -> "Exited"
        current.contains("no-show", ignoreCase = true) || current.contains("no show", ignoreCase = true) ||
            registration.contains("no-show", ignoreCase = true) || registration.contains("no show", ignoreCase = true) -> "No Show"
        registration.equals("Registered", ignoreCase = true) -> "Registered"
        current.isNotBlank() -> current
        registration.isNotBlank() -> registration
        else -> "Registered"
    }
}

internal fun OrganizerMvpAttendee.statusPalette(context: Context): Pair<Int, Int> {
    val (bgRes, textRes) = when (statusBucket()) {
        "Checked In" -> R.color.eventqr_badge_entered_bg to R.color.eventqr_badge_entered_text
        "Exited" -> R.color.eventqr_badge_default_bg to R.color.eventqr_badge_default_text
        "No Show" -> R.color.eventqr_badge_pending_bg to R.color.eventqr_badge_pending_text
        "Cancelled" -> R.color.eventqr_badge_cancelled_bg to R.color.eventqr_badge_cancelled_text
        else -> R.color.eventqr_badge_registered_bg to R.color.eventqr_badge_registered_text
    }
    return context.getColor(bgRes) to context.getColor(textRes)
}

internal fun transactionTypeLabel(value: String): String = when (value.trim().uppercase(Locale.ENGLISH)) {
    "ENTRY" -> "Entry"
    "ATTENDANCE" -> "Attendance"
    "BENEFIT_CLAIM" -> "Benefit Claim"
    "BOOTH_VISIT", "SESSION_VISIT" -> "Booth/Session Visit"
    "REWARD_REDEMPTION_SCAN", "REWARD_REDEMPTION" -> "Reward Redemption"
    "EXIT" -> "Exit"
    "ID_PRINT" -> "ID Printing"
    "REGISTRATION", "REGISTRATION_LOOKUP" -> "Registration"
    else -> value
}

internal fun OrganizerMvpAttendee.matchesOrganizerAttendeeQuery(query: String, filter: String): Boolean {
    val normalizedQuery = query.trim()
    val matchesFilter = when (filter) {
        "All" -> true
        "Registered" -> statusBucket().equals("Registered", ignoreCase = true)
        "Checked In" -> statusBucket().equals("Checked In", ignoreCase = true)
        "Exited" -> statusBucket().equals("Exited", ignoreCase = true)
        "No Show" -> statusBucket().equals("No Show", ignoreCase = true)
        else -> statusBucket().equals(filter, ignoreCase = true) || registrationStatus.equals(filter, ignoreCase = true)
    }
    if (!matchesFilter) return false
    if (normalizedQuery.isBlank()) return true
    return name.contains(normalizedQuery, ignoreCase = true) ||
        email.contains(normalizedQuery, ignoreCase = true) ||
        phone.contains(normalizedQuery, ignoreCase = true) ||
        id.contains(normalizedQuery, ignoreCase = true) ||
        registrationStatus.contains(normalizedQuery, ignoreCase = true) ||
        currentEventStatus.contains(normalizedQuery, ignoreCase = true) ||
        points.toString().contains(normalizedQuery, ignoreCase = true)
}

internal fun attendeeInitial(name: String): String = name.trim().firstOrNull()?.uppercase() ?: "?"

internal fun organizerEventDateLine(shortDate: String, eventTitle: String, venue: String): String {
    val rawDate = shortDate.trim()
    return rawDate
        .replace(Regex("T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?"), "")
        .replace(Regex("\\s*·.*$"), "")
        .trim()
        .ifBlank { eventTitle.trim() }
}
