package com.thedavelopers.eventqr.features.registrations

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.util.DateFormatters
import com.thedavelopers.eventqr.features.attendee.EXTRA_REGISTRATION_ID
import com.thedavelopers.eventqr.features.attendee.QrDisplayActivity
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse

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
        private val titleView: TextView = itemView.findViewById(R.id.txtRegisteredEventTitle)
        private val statusView: TextView = itemView.findViewById(R.id.txtRegisteredEventStatus)
        private val dateView: TextView = itemView.findViewById(R.id.txtRegisteredEventDate)
        private val locationView: TextView = itemView.findViewById(R.id.txtRegisteredEventLocation)
        private val pointsView: TextView = itemView.findViewById(R.id.txtPoints)
        private val btnTransactions: Button = itemView.findViewById(R.id.btnTransactionHistory)
        private val btnDetails: Button = itemView.findViewById(R.id.btnEventDetails)

        fun bind(registration: RegistrationResponse) {
            titleView.text = registration.eventTitle ?: "Registered event"
            RegistrationStatusBadgeStyler.bind(statusView, registration.status)

            val dateText = registration.eventStartAt?.let(DateFormatters::formatInstant) ?: "Date not specified"
            dateView.text = dateText

            val locationText = registration.eventLocation?.takeIf { it.isNotBlank() } ?: "Location not set"
            locationView.text = locationText

            // Points would ideally come from the registration or a separate balance call
            pointsView.text = "0 pts"

            btnTransactions.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, QrDisplayActivity::class.java).apply {
                    putExtra(EXTRA_REGISTRATION_ID, registration.registrationId.toString())
                    putExtra(com.thedavelopers.eventqr.features.attendee.EXTRA_QR_CREDENTIAL_ID, registration.qrCredentialId?.toString().orEmpty())
                }
                context.startActivity(intent)
            }

            btnTransactions.text = "View QR"
            btnDetails.visibility = View.VISIBLE
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