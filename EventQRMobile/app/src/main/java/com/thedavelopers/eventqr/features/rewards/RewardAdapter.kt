package com.thedavelopers.eventqr.features.rewards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.dto.RewardStatus
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardResponse

class RewardAdapter(
    private val onClick: (RewardResponse) -> Unit,
) : RecyclerView.Adapter<RewardAdapter.ViewHolder>() {

    private val items = mutableListOf<RewardResponse>()

    fun submitItems(newItems: List<RewardResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.txtRewardTitle)
        private val detailView: TextView = itemView.findViewById(R.id.txtRewardDetails)
        private val statusView: TextView = itemView.findViewById(R.id.txtRewardStatus)
        private val pointsView: TextView = itemView.findViewById(R.id.txtRewardPoints)
        private val stockView: TextView = itemView.findViewById(R.id.txtRewardStock)

        fun bind(item: RewardResponse) {
            titleView.text = item.name

            val description = item.description?.takeIf { it.isNotBlank() }
            detailView.visibility = if (description == null) View.GONE else View.VISIBLE
            detailView.text = description.orEmpty()

            val stockQuantity = item.stockQuantity
            val isUnavailable = item.status != RewardStatus.ACTIVE
            val isOutOfStock = !isUnavailable && stockQuantity != null && stockQuantity <= 0

            when {
                isUnavailable -> {
                    statusView.text = "Unavailable"
                    statusView.setBackgroundResource(R.drawable.bg_soft_gray_pill)
                    statusView.setTextColor(0xFF4B5563.toInt())
                }

                isOutOfStock -> {
                    statusView.text = "Out of Stock"
                    statusView.setBackgroundResource(R.drawable.bg_red_warning)
                    statusView.setTextColor(0xFFB91C1C.toInt())
                }

                else -> {
                    statusView.text = "Available"
                    statusView.setBackgroundResource(R.drawable.bg_green_pill)
                    statusView.setTextColor(0xFF065F46.toInt())
                }
            }

            pointsView.text = "${item.pointsRequired} pts"
            stockView.text = stockQuantity?.let { "$it left" } ?: "Stock unavailable"

            itemView.setOnClickListener { onClick(item) }
        }
    }
}