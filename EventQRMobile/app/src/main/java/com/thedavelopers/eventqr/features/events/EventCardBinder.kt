package com.thedavelopers.eventqr.features.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Context
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.dto.EventStatus

object EventCardBinder {

    fun inflate(
        context: Context,
        parent: ViewGroup?,
        title: String,
        status: String,
        day: String,
        month: String,
        time: String,
        location: String,
        count: Int,
        capacity: Int,
        percent: Int,
        onClick: (View) -> Unit,
    ): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_attendee_event, parent, false)
        bind(view, title, status, day, month, time, location, count, capacity, percent, onClick)
        return view
    }

    fun bind(
        view: View,
        title: String,
        status: String,
        day: String,
        month: String,
        time: String,
        location: String,
        count: Int,
        capacity: Int,
        percent: Int,
        onClick: (View) -> Unit,
    ) {
        val ctx = view.context
        val eventStatus = EventStatusBadgeStyler.fromLabel(status)

        view.findViewById<TextView>(R.id.txtAttendeeEventTitle).text =
            title.ifBlank { "Untitled event" }

        EventStatusBadgeStyler.bind(view.findViewById(R.id.txtAttendeeEventStatus), eventStatus)

        view.findViewById<TextView>(R.id.txtEventDay).text = day
        view.findViewById<TextView>(R.id.txtEventMonth).text = month
        view.findViewById<TextView>(R.id.txtAttendeeEventDateTime).text = time
        view.findViewById<TextView>(R.id.txtAttendeeEventLocation).text = location

        val regCount = "$count/$capacity registered"
        view.findViewById<TextView>(R.id.txtRegistrationCount).text = regCount
        view.findViewById<TextView>(R.id.txtRegistrationPercent).text = "$percent%"

        val progressBar = view.findViewById<ProgressBar>(R.id.pbRegistration)
        progressBar.progress = percent
        progressBar.progressDrawable = ctx.getDrawable(
            when (eventStatus) {
                EventStatus.ENDED -> R.drawable.pb_event_completed
                EventStatus.APPROVED -> R.drawable.pb_event_upcoming
                else -> R.drawable.pb_event_active
            },
        )

        view.setOnClickListener { onClick(it) }
    }
}
