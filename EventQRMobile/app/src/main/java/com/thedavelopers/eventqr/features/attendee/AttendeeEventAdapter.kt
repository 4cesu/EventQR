package com.thedavelopers.eventqr.features.attendee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.dto.EventStatus
import com.thedavelopers.eventqr.features.events.EventStatusBadgeStyler
import com.thedavelopers.eventqr.features.events.model.dto.AttendeeEventResponse

class AttendeeEventAdapter(
    private val onClick: (AttendeeEventResponse) -> Unit,
) : RecyclerView.Adapter<AttendeeEventAdapter.ViewHolder>() {

    private val items = mutableListOf<AttendeeEventResponse>()

    fun submitItems(newItems: List<AttendeeEventResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendee_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.txtAttendeeEventTitle)
        private val statusView: TextView = itemView.findViewById(R.id.txtAttendeeEventStatus)
        private val dateTimeView: TextView = itemView.findViewById(R.id.txtAttendeeEventDateTime)
        private val locationView: TextView = itemView.findViewById(R.id.txtAttendeeEventLocation)
        private val dayView: TextView = itemView.findViewById(R.id.txtEventDay)
        private val monthView: TextView = itemView.findViewById(R.id.txtEventMonth)
        private val regCountView: TextView = itemView.findViewById(R.id.txtRegistrationCount)
        private val regPercentView: TextView = itemView.findViewById(R.id.txtRegistrationPercent)
        private val progressBar: android.widget.ProgressBar = itemView.findViewById(R.id.pbRegistration)

        fun bind(item: AttendeeEventResponse) {
            val status = EventStatusBadgeStyler.resolve(item.status, item.eventStartAt, item.eventEndAt)
            val ctx = itemView.context

            titleView.text = item.title.ifBlank { "Untitled event" }

            EventStatusBadgeStyler.bind(statusView, status)

            if (item.eventStartAt != null) {
                val zonedDateTime = item.eventStartAt.atZone(java.time.ZoneId.of("Asia/Manila"))
                dayView.text = zonedDateTime.dayOfMonth.toString()
                monthView.text = zonedDateTime.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH).uppercase()

                val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH)
                dateTimeView.text = zonedDateTime.format(timeFormatter)
            } else {
                dayView.text = "--"
                monthView.text = "---"
                dateTimeView.text = "-"
            }

            locationView.text = item.location?.takeIf { it.isNotBlank() } ?: "Location not set"

            val capacity = item.capacity.coerceAtLeast(1)
            val current = item.currentAttendeeCount
            val percent = (current.toFloat() / capacity.toFloat() * 100).toInt().coerceIn(0, 100)

            regCountView.text = "$current/$capacity registered"
            regPercentView.text = "$percent%"
            progressBar.progress = percent
            progressBar.progressDrawable = ctx.getDrawable(
                when (status) {
                    EventStatus.ENDED -> R.drawable.pb_event_completed
                    EventStatus.APPROVED -> R.drawable.pb_event_upcoming
                    else -> R.drawable.pb_event_active
                },
            )

            itemView.setOnClickListener { onClick(item) }
        }
    }
}
