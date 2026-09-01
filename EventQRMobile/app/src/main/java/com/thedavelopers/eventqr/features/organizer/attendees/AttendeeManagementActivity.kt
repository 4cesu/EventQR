package com.thedavelopers.eventqr.features.organizer.attendees

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.organizer.AttendeeManagementAdapter
import com.thedavelopers.eventqr.features.organizer.EXTRA_EVENT_ID
import com.thedavelopers.eventqr.features.organizer.EXTRA_EVENT_TITLE
import com.thedavelopers.eventqr.features.organizer.NAV_ATTENDEES
import com.thedavelopers.eventqr.features.organizer.OrganizerMvpAttendee
import com.thedavelopers.eventqr.features.organizer.OrganizerMvpDataSource
import com.thedavelopers.eventqr.features.organizer.OrganizerMvpEvent
import com.thedavelopers.eventqr.features.organizer.OrganizerMvpLoad
import com.thedavelopers.eventqr.features.organizer.OrganizerRepository
import com.thedavelopers.eventqr.features.organizer.bottomNav
import com.thedavelopers.eventqr.features.organizer.eventSelector
import com.thedavelopers.eventqr.features.organizer.intentEventId
import com.thedavelopers.eventqr.features.organizer.organizerEventDateLine
import com.thedavelopers.eventqr.features.organizer.resolveSelectedEvent
import com.thedavelopers.eventqr.features.organizer.saveSelectedEventId
import com.thedavelopers.eventqr.features.organizer.selectedEventId
import com.thedavelopers.eventqr.features.organizer.statusBucket
import com.thedavelopers.eventqr.features.organizer.showMissingEventScreen
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

open class AttendeeManagementActivity : AppCompatActivity() {
    private lateinit var repository: OrganizerRepository
    private lateinit var selectedEvent: OrganizerMvpEvent
    private lateinit var adapter: AttendeeManagementAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtCheckedIn: TextView
    private lateinit var txtNoShow: TextView
    private lateinit var txtEventSelectorDate: TextView
    private lateinit var eventSelectorHost: LinearLayout
    private lateinit var bottomNavHost: LinearLayout

    private var attendees: List<OrganizerMvpAttendee> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendee_management)

        repository = OrganizerRepository(this)
        val eventId = intentEventId() ?: selectedEventId().takeIf { it.isNotBlank() }
            ?: return showMissingEventScreen("Attendee Management")
        selectedEvent = resolveSelectedEvent(repository.getApprovedOrganizerEvents(), eventId)
            ?: return showMissingEventScreen("Attendee Management")

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            startActivity(
                Intent(this, SearchAttendeesActivity::class.java)
                    .putExtra(EXTRA_EVENT_ID, selectedEvent.id)
                    .putExtra(EXTRA_EVENT_TITLE, selectedEvent.title)
            )
        }

        txtTotal = findViewById(R.id.txtTotalCount)
        txtCheckedIn = findViewById(R.id.txtCheckedInCount)
        txtNoShow = findViewById(R.id.txtNoShowCount)
        txtEventSelectorDate = findViewById(R.id.txtEventSelectorDate)
        swipeRefresh = findViewById(R.id.swipeRefreshAttendeeManagement)
        progressBar = findViewById(R.id.progressAttendees)
        emptyState = findViewById(R.id.txtAttendeesEmpty)
        eventSelectorHost = findViewById(R.id.layoutEventSelectorHost)
        bottomNavHost = findViewById(R.id.layoutBottomNavHost)

        swipeRefresh.setOnRefreshListener {
            refreshSelectedEventAttendees()
        }

        eventSelectorHost.addView(
            eventSelector(repository.getApprovedOrganizerEvents(), selectedEvent.id) { event ->
                selectedEvent = event
                repository.saveSelectedEventId(event.id)
                saveSelectedEventId(event.id)
                bindEventHeader()
                loadAttendees()
            }
        )

        adapter = AttendeeManagementAdapter { attendee -> openDetails(attendee) }
        findViewById<RecyclerView>(R.id.recyclerAttendees).apply {
            layoutManager = LinearLayoutManager(this@AttendeeManagementActivity)
            adapter = this@AttendeeManagementActivity.adapter
        }

        bottomNavHost.addView(bottomNav(NAV_ATTENDEES))
        bindEventHeader()
        loadAttendees()
    }

    private fun refreshSelectedEventAttendees() {
        val currentEventId = selectedEvent.id
        val latestSelectedEvent = resolveSelectedEvent(repository.getApprovedOrganizerEvents(), currentEventId)
        if (latestSelectedEvent != null) {
            selectedEvent = latestSelectedEvent
            repository.saveSelectedEventId(latestSelectedEvent.id)
            saveSelectedEventId(latestSelectedEvent.id)
            bindEventHeader()
        }
        loadAttendees()
    }

    private fun loadAttendees() {
        if (!swipeRefresh.isRefreshing) {
            progressBar.visibility = View.VISIBLE
        }
        MainScope().launch {
            val eventIdAtRequestTime = selectedEvent.id
            val load = repository.loadAttendeesForMvp(eventIdAtRequestTime)
            if (eventIdAtRequestTime != selectedEvent.id) {
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
                return@launch
            }
            attendees = load.data
            swipeRefresh.isRefreshing = false
            progressBar.visibility = View.GONE
            render(load)
        }
    }

    private fun bindEventHeader() {
        val dateLine = organizerEventDateLine(selectedEvent.shortDate, selectedEvent.title, selectedEvent.venue)
        txtEventSelectorDate.text = if (dateLine.isBlank()) selectedEvent.title else "${selectedEvent.title} · $dateLine"
    }

    private fun render(load: OrganizerMvpLoad<List<OrganizerMvpAttendee>>) {
        val checkedIn = attendees.count { it.statusBucket().equals("Checked In", ignoreCase = true) }
        val noShow = attendees.count { it.statusBucket().equals("No Show", ignoreCase = true) }

        txtTotal.text = attendees.size.toString()
        txtCheckedIn.text = checkedIn.toString()
        txtNoShow.text = noShow.toString()

        adapter.submitItems(attendees)
        emptyState.visibility = if (attendees.isEmpty()) View.VISIBLE else View.GONE
        emptyState.text = when {
            load.source == OrganizerMvpDataSource.ERROR -> load.message ?: "Attendees could not be loaded."
            attendees.isEmpty() -> "No attendees registered yet."
            else -> ""
        }
    }

    private fun openDetails(attendee: OrganizerMvpAttendee) {
        startActivity(
            Intent(this, AttendeeDetailsActivity::class.java)
                .putExtra(EXTRA_EVENT_ID, selectedEvent.id)
                .putExtra(EXTRA_EVENT_TITLE, selectedEvent.title)
                .putExtra(SearchAttendeesActivity.EXTRA_ATTENDEE_ID, attendee.id)
        )
    }
}
