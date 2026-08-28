package com.thedavelopers.eventqr.features.registrations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse
import java.util.UUID

class RegistrationAdapter(
    private val onClick: ((RegistrationResponse) -> Unit)? = null,
    private val onSelectionChanged: ((Int) -> Unit)? = null,
) : RecyclerView.Adapter<RegistrationAdapter.ViewHolder>() {

    private val items = mutableListOf<RegistrationResponse>()
    private var selectionMode = false
    private val selectedIds = mutableSetOf<UUID>()

    fun submitItems(newItems: List<RegistrationResponse>) {
        items.clear()
        items.addAll(newItems)
        // Drop selections that no longer exist in the refreshed list.
        selectedIds.retainAll(items.map { it.attendeeUserId }.toSet())
        notifyDataSetChanged()
    }

    fun setSelectionMode(enabled: Boolean) {
        selectionMode = enabled
        if (!enabled) clearSelectionInternal(notify = true)
        notifyDataSetChanged()
    }

    fun isSelectionMode(): Boolean = selectionMode

    fun isSelected(registration: RegistrationResponse): Boolean = registration.attendeeUserId in selectedIds

    fun getSelectedItems(): List<RegistrationResponse> =
        items.filter { it.attendeeUserId in selectedIds }

    fun toggleSelection(registration: RegistrationResponse) {
        if (!selectionMode) return
        if (!selectedIds.add(registration.attendeeUserId)) selectedIds.remove(registration.attendeeUserId)
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedIds.size)
    }

    fun toggleSelectAll() {
        val allIds = items.filter { it.qrCredentialId != null }.map { it.attendeeUserId }.toSet()
        selectedIds.clear()
        selectedIds.addAll(allIds)
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedIds.size)
    }

    fun isAllSelected(): Boolean = items.isNotEmpty() && selectedIds.size == items.size

    fun clearSelection() {
        if (selectionMode) return
        clearSelectionInternal(notify = true)
    }

    private fun clearSelectionInternal(notify: Boolean) {
        if (selectedIds.isEmpty()) return
        selectedIds.clear()
        if (notify) {
            notifyDataSetChanged()
            onSelectionChanged?.invoke(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registration, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkView: CheckBox = itemView.findViewById(R.id.chkRegistrationSelect)
        private val avatarView: TextView = itemView.findViewById(R.id.txtRegistrationAvatar)
        private val titleView: TextView = itemView.findViewById(R.id.txtRegistrationTitle)
        private val detailView: TextView = itemView.findViewById(R.id.txtRegistrationDetails)
        private val statusView: TextView = itemView.findViewById(R.id.txtRegistrationStatus)
        private val pointsView: TextView = itemView.findViewById(R.id.txtRegistrationPoints)

        fun bind(item: RegistrationResponse) {
            val name = item.attendeeName.ifBlank { "Attendee" }
            avatarView.text = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"
            titleView.text = name
            detailView.text = item.attendeeEmail.ifBlank { "No email provided" }
            pointsView.text = "${item.pointsEarned} pts"
            RegistrationStatusBadgeStyler.bind(statusView, item.status)

            checkView.visibility = if (selectionMode) View.VISIBLE else View.GONE
            checkView.isChecked = isSelected(item)
            checkView.isEnabled = item.qrCredentialId != null

            (itemView as? androidx.cardview.widget.CardView)?.setCardBackgroundColor(
                if (selectionMode && isSelected(item)) 0xFFE0E7FF.toInt() else android.graphics.Color.WHITE
            )

            itemView.setOnClickListener {
                if (selectionMode) {
                    toggleSelection(item)
                } else {
                    onClick?.invoke(item)
                }
            }
            checkView.setOnClickListener {
                if (checkView.isChecked) selectedIds.add(item.attendeeUserId)
                else selectedIds.remove(item.attendeeUserId)
                (itemView as? androidx.cardview.widget.CardView)?.setCardBackgroundColor(
                    if (isSelected(item)) 0xFFE0E7FF.toInt() else android.graphics.Color.WHITE
                )
                onSelectionChanged?.invoke(selectedIds.size)
            }
        }
    }
}
