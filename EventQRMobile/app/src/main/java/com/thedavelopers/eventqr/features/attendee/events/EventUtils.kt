package com.thedavelopers.eventqr.features.attendee

import com.thedavelopers.eventqr.features.events.EventStatusBadgeStyler
import com.thedavelopers.eventqr.features.events.model.dto.AttendeeEventResponse

fun computedStatusLabel(item: AttendeeEventResponse): String {
    val status = EventStatusBadgeStyler.resolve(item.status, item.eventStartAt, item.eventEndAt)
    return EventStatusBadgeStyler.displayLabel(status)
}
