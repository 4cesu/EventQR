package com.thedavelopers.eventqr.features.events

import android.content.Context
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.dto.EventStatus
import java.time.Instant

/**
 * Single source of truth for mapping an event's lifecycle [EventStatus] to its
 * display label and color/badge styling across the whole app (Organizer,
 * Attendee, Staff, Admin screens).
 *
 * Mirrors [com.thedavelopers.eventqr.features.registrations.RegistrationStatusBadgeStyler];
 * that class handles registration statuses and is intentionally out of scope here.
 */
object EventStatusBadgeStyler {

    @ColorRes
    fun textColorRes(status: EventStatus): Int = when (status) {
        EventStatus.APPROVED -> R.color.eventqr_event_status_upcoming
        EventStatus.ACTIVE -> R.color.eventqr_event_status_active
        EventStatus.ENDED -> R.color.eventqr_event_status_completed
        EventStatus.DRAFT -> R.color.eventqr_event_status_draft
        EventStatus.PENDING_REVIEW -> R.color.eventqr_event_status_pending_review
        EventStatus.REJECTED -> R.color.eventqr_event_status_rejected
        EventStatus.CANCELLED -> R.color.eventqr_event_status_cancelled
    }

    @DrawableRes
    fun backgroundRes(status: EventStatus): Int = when (status) {
        EventStatus.APPROVED -> R.drawable.bg_event_badge_upcoming
        EventStatus.ACTIVE -> R.drawable.bg_event_badge_active
        EventStatus.ENDED -> R.drawable.bg_event_badge_completed
        EventStatus.DRAFT -> R.drawable.bg_event_badge_draft
        EventStatus.PENDING_REVIEW -> R.drawable.bg_event_badge_pending_review
        EventStatus.REJECTED -> R.drawable.bg_event_badge_rejected
        EventStatus.CANCELLED -> R.drawable.bg_event_badge_cancelled
    }

    /** Saturated primary/accent color for a status (used for top strips, text, progress bars). */
    fun primaryColor(context: Context, status: EventStatus): Int =
        ContextCompat.getColor(context, textColorRes(status))

    /** Applies label + pill background + text color to an existing TextView. */
    fun bind(view: TextView, status: EventStatus) {
        view.text = displayLabel(status)
        view.setBackgroundResource(backgroundRes(status))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes(status)))
    }

    fun displayLabel(status: EventStatus): String = when (status) {
        EventStatus.DRAFT -> "Draft"
        EventStatus.PENDING_REVIEW -> "Pending Review"
        EventStatus.APPROVED -> "Upcoming"
        EventStatus.REJECTED -> "Rejected"
        EventStatus.ACTIVE -> "Active"
        EventStatus.ENDED -> "Completed"
        EventStatus.CANCELLED -> "Cancelled"
    }

    /**
     * Parses a raw backend status string, display label, or raw enum name back into an
     * [EventStatus]. Used by screens that receive a status as a string (e.g. the organizer
     * MVP events) so they can still reuse the centralized styling. Unknown/empty input
     * defaults to [EventStatus.APPROVED].
     */
    fun fromLabel(label: String?): EventStatus {
        val l = label?.lowercase() ?: ""
        return when {
            l.contains("completed") || l.contains("ended") -> EventStatus.ENDED
            l.contains("active") || l.contains("ongoing") -> EventStatus.ACTIVE
            l.contains("draft") -> EventStatus.DRAFT
            l.contains("pending") || l.contains("review") -> EventStatus.PENDING_REVIEW
            l.contains("reject") -> EventStatus.REJECTED
            l.contains("cancel") -> EventStatus.CANCELLED
            else -> EventStatus.APPROVED
        }
    }

    /**
     * Date-derived fallback for screens (Attendee list/dashboard/detail) that derive the
     * shown state from eventStartAt/eventEndAt rather than from the backend [EventStatus].
     *
     * FLAGGED DEVIATION: the backend EventStatus enum should be authoritative. This helper is
     * provided so existing date-based flows can reuse the centralized styling without
     * duplication; it maps "before start" -> [EventStatus.APPROVED] (Upcoming),
     * "past end" -> [EventStatus.ENDED] (Completed), otherwise [EventStatus.ACTIVE].
     */
    fun fromDates(startAt: Instant?, endAt: Instant?, now: Instant = Instant.now()): EventStatus {
        val completed = endAt != null && endAt.isBefore(now)
        val upcoming = startAt != null && startAt.isAfter(now)
        return when {
            completed -> EventStatus.ENDED
            upcoming -> EventStatus.APPROVED
            else -> EventStatus.ACTIVE
        }
    }

    /**
     * Resolves the backend [EventStatus] when present, falling back to a date-derived
     * status only when the backend enum is null/missing (toward the flagged deviation above).
     * The backend enum is the authoritative source.
     */
    fun resolve(status: EventStatus?, startAt: Instant?, endAt: Instant?, now: Instant = Instant.now()): EventStatus =
        status ?: fromDates(startAt, endAt, now)
}
