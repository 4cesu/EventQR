package com.thedavelopers.eventqr.features.organizer.transactions

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.features.organizer.*
import com.thedavelopers.eventqr.features.organizer.attendees.SearchAttendeesActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

open class TransactionLogsActivity : AppCompatActivity() {
    private lateinit var repository: OrganizerRepository
    private lateinit var selectedEvent: OrganizerMvpEvent
    private lateinit var list: LinearLayout
    private var attendeeId: String? = null
    private var logsSource: OrganizerMvpLoad<List<OrganizerMvpTransaction>> =
        OrganizerMvpLoad(emptyList(), OrganizerMvpDataSource.ERROR, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = OrganizerRepository(this)
        val eventId = intentEventId() ?: return showMissingEventScreen("Event Logs")
        val selectableEvents = repository.getApprovedOrganizerEvents()
        selectedEvent = resolveSelectedEvent(selectableEvents, eventId) ?: return showMissingEventScreen("Event Logs")
        attendeeId = intent.getStringExtra(SearchAttendeesActivity.EXTRA_ATTENDEE_ID)
        val content = organizerShell("Event Logs", null, NAV_LOGS, showBack = true)

        if (selectableEvents.isNotEmpty()) {
            content.addView(eventSelector(selectableEvents, selectedEvent.id) {
                selectedEvent = it
                repository.saveSelectedEventId(it.id)
                saveSelectedEventId(it.id)
                loadLogs()
            })
        }

        list = LinearLayout(this).apply {
            id = com.thedavelopers.eventqr.R.id.tlg_list
            orientation = LinearLayout.VERTICAL
        }
        content.addView(list)
        list.addView(loadingState("Loading event logs..."))
        loadLogs()
    }

    private fun loadLogs() {
        MainScope().launch {
            logsSource = repository.loadTransactionsForMvp(selectedEvent.id, selectedEvent.title)
            render()
        }
    }

    private fun isApproved(log: OrganizerMvpTransaction): Boolean =
        log.status.equals("Approved", true) || log.status.equals("Successful", true)

    private fun normalizedStatus(log: OrganizerMvpTransaction): String =
        if (isApproved(log)) "Approved" else log.status

    private fun render() {
        val allLogs = logsSource.data
        val logs = attendeeId?.let { id -> allLogs.filter { it.attendeeId == id } } ?: allLogs
        list.removeAllViews()
        if (allLogs.isEmpty() && logsSource.source == OrganizerMvpDataSource.ERROR) {
            list.addView(errorState(logsSource.message ?: "Event logs could not be loaded.") { loadLogs() })
            return
        }
        if (logs.isEmpty()) {
            val message = if (attendeeId == null) "No event logs for this event yet." else "No event logs for this attendee yet."
            list.addView(emptyState(message))
            return
        }
        logs.forEach { list.addView(logCard(it)) }
    }

    private fun logCard(log: OrganizerMvpTransaction): LinearLayout =
        card().apply {
            val status = normalizedStatus(log)
            val top = row()
            top.addView(text(log.attendeeName, 16, true).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            top.addView(badge(status))
            addView(top)
            addView(text("${log.type} • $status", 12, true, if (status == "Rejected") ERROR else SUCCESS))
            addView(text(log.eventTitle, 12, false, MUTED))
            addView(text("Attendee: ${log.attendeeEmail.ifBlank { "No email" }}", 12, false, MUTED))
            addView(text("Staff: ${log.staffName}", 12, false, MUTED))
            addView(text("Staff email: ${log.staffEmail.ifBlank { "No email" }}", 12, false, MUTED))
            addView(text(log.timestamp, 12, false, MUTED))
            if (status == "Rejected") addView(text("Reason: ${log.reason}", 12, true, ERROR))
        }
}