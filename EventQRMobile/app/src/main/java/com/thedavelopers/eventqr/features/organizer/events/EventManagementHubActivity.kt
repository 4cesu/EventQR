package com.thedavelopers.eventqr.features.organizer.events

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.features.events.EventStatusBadgeStyler
import com.thedavelopers.eventqr.features.organizer.*
import com.thedavelopers.eventqr.features.organizer.scanpurposes.ManageScanPurposesActivity
import com.thedavelopers.eventqr.features.organizer.staff.ManageUsersActivity
import com.thedavelopers.eventqr.features.organizer.transactions.TransactionRulesActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

open class EventManagementHubActivity : AppCompatActivity() {
    private lateinit var repository: OrganizerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = OrganizerRepository(this)
    }

    // Reload on every return to this screen (e.g., after saving edits in
    // EditEventDetailsActivity) so the Registered/Capacity/Available summary never shows
    // stale numbers.
    override fun onResume() {
        super.onResume()
        val eventId = intentEventId() ?: return showMissingEventScreen("Event Management")
        renderEvent(eventId)
    }

    private fun renderEvent(eventId: String) {
        val content = organizerShell("Event Management", showBack = true)
        content.addView(loadingState("Loading event details..."))

        MainScope().launch {
            val load = repository.loadEventForMvp(eventId)
            val event = load.data
            content.removeAllViews()
            if (event == null) {
                dataSourceBanner(load)?.let { content.addView(it) }
                content.addView(
                    if (load.source == OrganizerMvpDataSource.ERROR) {
                        errorState(load.message ?: "Event details could not be loaded.") { recreate() }
                    } else {
                        emptyState("Event not found or not available for organizer management.", "Open My Events") {
                            openOrganizerPage(ManageEventsActivity::class.java)
                        }
                    },
                )
                return@launch
            }

            content.setPadding(0, 0, 0, dp(18))

            val registeredCount = event.currentAttendeeCount.coerceAtLeast(0)
            val capacity = event.capacity.coerceAtLeast(0)
            val available = (capacity - registeredCount).coerceAtLeast(0)

            content.addView(LinearLayout(this@EventManagementHubActivity).apply {
                id = com.thedavelopers.eventqr.R.id.emh_header_banner
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(120),
                )
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(Color.parseColor("#5A45F2"), Color.parseColor("#9B8CF5")),
                )
                setPadding(dp(20), 0, dp(20), 0)
                gravity = android.view.Gravity.CENTER_VERTICAL

                addView(text(
                    event.lifecycleStatus(),
                    11,
                    true,
                    EventStatusBadgeStyler.primaryColor(this@EventManagementHubActivity, EventStatusBadgeStyler.fromLabel(event.status)),
                ).apply {
                    id = com.thedavelopers.eventqr.R.id.emh_status_badge
                    setPadding(dp(12), dp(4), dp(12), dp(4))
                    background = rounded(Color.WHITE, 16, null, density = resources.displayMetrics.density)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                })

                addView(text(event.title, 21, true, Color.WHITE).apply {
                    id = com.thedavelopers.eventqr.R.id.emh_event_title
                    setPadding(0, dp(8), 0, 0)
                })
            })

            val body = LinearLayout(this@EventManagementHubActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), 0)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            content.addView(body)

            dataSourceBanner(load)?.let { body.addView(it) }

            body.addView(row().apply {
                id = com.thedavelopers.eventqr.R.id.emh_stats_row
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                addView(summaryCard("Registered", formatCount(registeredCount)).apply {
                    id = com.thedavelopers.eventqr.R.id.emh_stat_registered
                })
                addView(summaryCard("Capacity", formatCount(capacity), Color.parseColor("#94A3B8")).apply {
                    id = com.thedavelopers.eventqr.R.id.emh_stat_capacity
                })
                addView(summaryCard("Available", formatCount(available), SUCCESS).apply {
                    id = com.thedavelopers.eventqr.R.id.emh_stat_available
                })
            })

            body.addView(section("Event Management").apply {
                id = com.thedavelopers.eventqr.R.id.emh_section_title
                setPadding(dp(2), dp(20), dp(2), dp(10))
            })

            // UC-20 edit lock: once an event is Active (ongoing) or Completed the Organizer can
            // no longer edit its details, so the entry is relabeled to view-only ("View Event
            // Details") instead of being hidden - the screen opens fully read-only (backend also
            // enforces a 409 if a stale client still tries to edit).
            val canEdit = event.lifecycleStatus() == "Upcoming"
            val editLabel = if (canEdit) "Edit Event Details" else "View Event Details"
            val menuItems = buildList {
                add(Triple(editLabel, if (canEdit) com.thedavelopers.eventqr.R.drawable.ic_edit_pencil else com.thedavelopers.eventqr.R.drawable.ic_event_request, EditEventDetailsActivity::class.java))
                add(Triple("Staff Assignment", com.thedavelopers.eventqr.R.drawable.ic_admin_users, ManageUsersActivity::class.java))
                add(Triple("Scan Purposes", com.thedavelopers.eventqr.R.drawable.ic_scan, ManageScanPurposesActivity::class.java))
                add(Triple("Transaction Rules", com.thedavelopers.eventqr.R.drawable.ic_admin_shield, TransactionRulesActivity::class.java))
                add(Triple("ID Display Settings", com.thedavelopers.eventqr.R.drawable.ic_id, com.thedavelopers.eventqr.features.organizer.idtemplate.IdTemplateSettingsActivity::class.java))
            }

            // Colors keyed by label, not position: inserting/reordering rows must never
            // reshuffle the visual identity of the existing entries.
            menuItems.forEachIndexed { index, (label, icon, target) ->
                val (iconTint, iconBg) = when (label) {
                    "Edit Event Details", "View Event Details" -> Color.parseColor("#2563EB") to Color.parseColor("#DBEAFE")
                    "Staff Assignment" -> Color.parseColor("#4F46E5") to Color.parseColor("#E0E7FF")
                    "Scan Purposes" -> Color.parseColor("#06B6D4") to Color.parseColor("#CFFAFE")
                    "Transaction Rules" -> Color.parseColor("#F59E0B") to Color.parseColor("#FEF3C7")
                    "ID Display Settings" -> Color.parseColor("#10B981") to Color.parseColor("#D1FAE5")
                    else -> error("Unexpected Event Management hub row: $label")
                }
                val menuId = when (label) {
                    "Edit Event Details", "View Event Details" -> com.thedavelopers.eventqr.R.id.emh_menu_edit
                    "Staff Assignment" -> com.thedavelopers.eventqr.R.id.emh_menu_staff
                    "Scan Purposes" -> com.thedavelopers.eventqr.R.id.emh_menu_scan
                    "Transaction Rules" -> com.thedavelopers.eventqr.R.id.emh_menu_transaction
                    "ID Display Settings" -> com.thedavelopers.eventqr.R.id.emh_menu_id
                    else -> View.generateViewId()
                }
                body.addView(
                    menuCard(
                        label = label,
                        iconRes = icon,
                        iconTint = iconTint,
                        iconBg = iconBg,
                    ) { openOrganizerPage(target, event.id, event.title, viewOnly = label == "View Event Details") }.apply {
                        id = menuId
                    },
                )
            }
        }
    }
}
