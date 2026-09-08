package com.thedavelopers.eventqr.features.registrations

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.attendee.EXTRA_REGISTRATION_ID
import com.thedavelopers.eventqr.features.attendee.QrDisplayActivity
import com.thedavelopers.eventqr.features.events.EventStatusBadgeStyler
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class RegisteredEventAdapter : RecyclerView.Adapter<RegisteredEventAdapter.ViewHolder>() {

    private val items = mutableListOf<RegistrationResponse>()

    fun submitItems(newItems: List<RegistrationResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registered_event, parent, false)
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
        private val dateBadgeView: View = itemView.findViewById(R.id.layoutEventDate)
        private val btnQR: Button = itemView.findViewById(R.id.btnTransactionHistory)
        private val btnDetails: Button = itemView.findViewById(R.id.btnEventDetails)

        fun bind(registration: RegistrationResponse) {
            val status = EventStatusBadgeStyler.resolve(null, registration.eventStartAt, registration.eventEndAt)

            titleView.text = registration.eventTitle ?: "Registered event"

            EventStatusBadgeStyler.bind(statusView, status)
            dateBadgeView.setBackgroundResource(EventStatusBadgeStyler.dateBadgeRes(status))

            if (registration.eventStartAt != null) {
                val zonedDateTime = registration.eventStartAt.atZone(ZoneId.of("Asia/Manila"))
                dayView.text = zonedDateTime.dayOfMonth.toString()
                monthView.text = zonedDateTime.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
                dateTimeView.text = zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
            } else {
                dayView.text = "--"
                monthView.text = "---"
                dateTimeView.text = "-"
            }

            locationView.text = registration.eventLocation?.takeIf { it.isNotBlank() } ?: "Location not set"

            btnQR.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, QrDisplayActivity::class.java).apply {
                    putExtra(EXTRA_REGISTRATION_ID, registration.registrationId.toString())
                    putExtra(com.thedavelopers.eventqr.features.attendee.EXTRA_QR_CREDENTIAL_ID, registration.qrCredentialId?.toString().orEmpty())
                }
                context.startActivity(intent)
            }

            btnDetails.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, com.thedavelopers.eventqr.features.attendee.EventDetailActivity::class.java).apply {
                    putExtra(com.thedavelopers.eventqr.features.attendee.EXTRA_EVENT_ID, registration.eventId.toString())
                    putExtra(com.thedavelopers.eventqr.features.attendee.EXTRA_EVENT_TITLE, registration.eventTitle.orEmpty())
                }
                context.startActivity(intent)
            }
        }
    }
}