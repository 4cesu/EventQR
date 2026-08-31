package com.thedavelopers.eventqr.features.transactions

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionLogAdapter : RecyclerView.Adapter<TransactionLogAdapter.ViewHolder>() {

    private val items = mutableListOf<TransactionResponse>()
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.ENGLISH)
        .withZone(ZoneId.of("Asia/Manila"))

    fun submitItems(newItems: List<TransactionResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameView: TextView = itemView.findViewById(R.id.txtUserName)
        private val eventNameView: TextView = itemView.findViewById(R.id.txtEventName)
        private val timeView: TextView = itemView.findViewById(R.id.txtTransactionTime)
        private val purposeNameView: TextView = itemView.findViewById(R.id.txtPurposeName)
        private val purposeIconView: ImageView = itemView.findViewById(R.id.imgPurposeIcon)
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.iconContainer)
        private val pointsView: TextView = itemView.findViewById(R.id.txtPointsDelta)
        private val statusBadgeView: TextView = itemView.findViewById(R.id.txtStatusBadge)

        fun bind(item: TransactionResponse) {
            val isSuccess = item.transactionResult.name == "APPROVED" || item.transactionResult.name == "SUCCESS"
            val points = item.pointsDelta

            userNameView.text = item.attendeeName?.takeIf { it.isNotBlank() } ?: "Attendee"
            eventNameView.text = item.eventTitle?.takeIf { it.isNotBlank() } ?: "Event"
            purposeNameView.text = item.scanPurposeName?.takeIf { it.isNotBlank() } ?: formatType(item.transactionType.name)
            timeView.text = formatTime(item.scannedAt)

            iconContainer.setBackgroundResource(if (isSuccess) R.drawable.bg_transaction_earned_icon else R.drawable.bg_transaction_redeemed_icon)
            purposeIconView.setImageResource(if (isSuccess) R.drawable.ic_staff_check else R.drawable.ic_staff_close)
            purposeIconView.imageTintList = ColorStateList.valueOf(
                if (isSuccess) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
            )

            statusBadgeView.visibility = View.VISIBLE
            statusBadgeView.text = if (isSuccess) "Success" else "Rejected"
            statusBadgeView.setBackgroundResource(if (isSuccess) R.drawable.bg_success_chip else R.drawable.bg_red_warning)
            statusBadgeView.setTextColor(if (isSuccess) Color.parseColor("#15803D") else Color.parseColor("#B91C1C"))

            if (points == 0) {
                pointsView.visibility = View.INVISIBLE
                pointsView.text = "0 pts"
            } else {
                pointsView.visibility = View.VISIBLE
                pointsView.text = if (points > 0) "+$points pts" else "$points pts"
                pointsView.setTextColor(if (points > 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
            }
        }
    }

    private fun formatTime(value: Instant?): String = value?.let { timeFormatter.format(it) } ?: "--:--"

    private fun formatType(value: String): String = value
        .lowercase(Locale.US)
        .split('_')
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase(Locale.US) } }
}