package com.thedavelopers.eventqr.features.staff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.dto.EventStatus
import com.thedavelopers.eventqr.features.events.EventStatusBadgeStyler
import com.thedavelopers.eventqr.features.staff.model.dto.StaffAssignedEventResponse
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class StaffAssignedEventAdapter(
    private val onScanClick: (StaffAssignedEventResponse) -> Unit,
    private val onAttendeesClick: (StaffAssignedEventResponse) -> Unit,
) : RecyclerView.Adapter<StaffAssignedEventAdapter.ViewHolder>() {

    private val items = mutableListOf<StaffAssignedEventResponse>()
    private val manilaZone = ZoneId.of("Asia/Manila")

    fun submitItems(newItems: List<StaffAssignedEventResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff_assigned_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.txtStaffEventTitle)
        private val statusView: TextView = itemView.findViewById(R.id.txtStaffEventStatus)
        private val dateTimeView: TextView = itemView.findViewById(R.id.txtStaffEventDateTime)
        private val locationView: TextView = itemView.findViewById(R.id.txtStaffEventLocation)
        private val dayView: TextView = itemView.findViewById(R.id.txtEventDay)
        private val monthView: TextView = itemView.findViewById(R.id.txtEventMonth)
        private val dateBlock: LinearLayout = itemView.findViewById(R.id.layoutEventDate)
        private val btnScan: LinearLayout = itemView.findViewById(R.id.btnScan)
        private val btnAttendees: LinearLayout = itemView.findViewById(R.id.btnAttendees)

        fun bind(item: StaffAssignedEventResponse) {
            val ctx = itemView.context
            val resolvedStatus = EventStatusBadgeStyler.resolve(
                item.status, item.eventStartAt, item.eventEndAt,
            )

            titleView.text = item.title.ifBlank { "Untitled Event" }
            EventStatusBadgeStyler.bind(statusView, resolvedStatus)

            bindDateBlock(resolvedStatus)
            bindDateTime(item)
            bindLocation(item)

            btnScan.setOnClickListener { onScanClick(item) }
            btnAttendees.setOnClickListener { onAttendeesClick(item) }
        }

        private fun bindDateBlock(status: EventStatus) {
            val ctx = itemView.context
            dateBlock.background = ContextCompat.getDrawable(
                ctx,
                R.drawable.bg_event_date_upcoming,
            )
            dayView.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
            monthView.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
        }

        private fun bindDateTime(item: StaffAssignedEventResponse) {
            if (item.eventStartAt != null) {
                val zdt = item.eventStartAt.atZone(manilaZone)
                dayView.text = zdt.dayOfMonth.toString()
                monthView.text = zdt.month.getDisplayName(
                    java.time.format.TextStyle.SHORT, Locale.ENGLISH,
                ).uppercase()
                val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
                dateTimeView.text = zdt.format(timeFmt)
            } else {
                dayView.text = "--"
                monthView.text = "---"
                dateTimeView.text = "Date not set"
            }
        }

        private fun bindLocation(item: StaffAssignedEventResponse) {
            locationView.text = item.location?.takeIf { it.isNotBlank() } ?: "Location not set"
        }
    }
}
