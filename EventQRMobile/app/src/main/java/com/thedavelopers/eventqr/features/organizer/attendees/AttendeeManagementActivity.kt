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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

open class AttendeeManagementActivity : AppCompatActivity() {
    private lateinit var repository: OrganizerRepository
    private var selectedEvent: OrganizerMvpEvent? = null
    private lateinit var adapter: AttendeeManagementAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var skeletonLoading: View
    private lateinit var emptyStateLayout: View
    private lateinit var emptyStateTitle: TextView
    private lateinit var emptyStateSub: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtCheckedIn: TextView
    private lateinit var txtNoShow: TextView
    private lateinit var txtEventSelectorDate: TextView
    private lateinit var eventSelectorHost: LinearLayout
    private lateinit var bottomNavHost: LinearLayout
    private lateinit var currentEventLabel: TextView
    private lateinit var cardAttendeeStats: View

    private var attendees: List<OrganizerMvpAttendee> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendee_management)

        repository = OrganizerRepository(this)

        txtTotal = findViewById(R.id.txtTotalCount)
        txtCheckedIn = findViewById(R.id.txtCheckedInCount)
        txtNoShow = findViewById(R.id.txtNoShowCount)
        txtEventSelectorDate = findViewById(R.id.txtEventSelectorDate)
        swipeRefresh = findViewById(R.id.swipeRefreshAttendeeManagement)
        progressBar = findViewById(R.id.progressAttendees)
        skeletonLoading = findViewById(R.id.skeletonLoading)
        emptyStateLayout = findViewById(R.id.layoutAttendeesEmpty)
        emptyStateTitle = findViewById(R.id.txtAttendeesEmptyTitle)
        emptyStateSub = findViewById(R.id.txtAttendeesEmptySub)
        eventSelectorHost = findViewById(R.id.layoutEventSelectorHost)
        bottomNavHost = findViewById(R.id.layoutBottomNavHost)
        currentEventLabel = findViewById(R.id.txtCurrentEventLabel)
        cardAttendeeStats = findViewById(R.id.cardAttendeeStats)

        bottomNavHost.addView(bottomNav(NAV_ATTENDEES))

        swipeRefresh.setOnRefreshListener {
            refreshSelectedEventAttendees()
        }

        lifecycleScope.launch {
            val events = repository.getApprovedOrganizerEvents()

            if (events.isEmpty()) {
                showNoEventsAvailableState()
                return@launch
            }

            adapter = AttendeeManagementAdapter { attendee -> openDetails(attendee) }
            findViewById<RecyclerView>(R.id.recyclerAttendees).apply {
                layoutManager = LinearLayoutManager(this@AttendeeManagementActivity)
                adapter = this@AttendeeManagementActivity.adapter
            }

            eventSelectorHost.addView(
                eventSelector(events, selectedEvent?.id.orEmpty()) { event ->
                    selectedEvent = event
                    repository.saveSelectedEventId(event.id)
                    saveSelectedEventId(event.id)
                    restoreNormalUi()
                    bindEventHeader()
                    loadAttendees()
                }
            )

            val eventId = intentEventId() ?: selectedEventId().takeIf { it.isNotBlank() }
            val resolvedEvent = if (eventId != null) {
                resolveSelectedEvent(events, eventId)
            } else null

            if (resolvedEvent != null) {
                selectedEvent = resolvedEvent
                findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
                    startActivity(
                        Intent(this@AttendeeManagementActivity, SearchAttendeesActivity::class.java)
                            .putExtra(EXTRA_EVENT_ID, resolvedEvent.id)
                            .putExtra(EXTRA_EVENT_TITLE, resolvedEvent.title)
                    )
                }
                bindEventHeader()
                loadAttendees()
            } else {
                showNoEventEmptyState()
            }
        }
    }

    private fun refreshSelectedEventAttendees() {
        val event = selectedEvent ?: return
        lifecycleScope.launch {
            val currentEventId = event.id
            val latestSelectedEvent = resolveSelectedEvent(repository.getApprovedOrganizerEvents(), currentEventId)
            if (latestSelectedEvent != null) {
                selectedEvent = latestSelectedEvent
                repository.saveSelectedEventId(latestSelectedEvent.id)
                saveSelectedEventId(latestSelectedEvent.id)
                bindEventHeader()
            }
            loadAttendees()
        }
    }

    private fun loadAttendees() {
        val event = selectedEvent ?: return
        if (!swipeRefresh.isRefreshing) {
            progressBar.visibility = View.GONE
            skeletonLoading.visibility = View.VISIBLE
        }
        MainScope().launch {
            val eventIdAtRequestTime = event.id
            val load = repository.loadAttendeesForMvp(eventIdAtRequestTime)
            if (eventIdAtRequestTime != selectedEvent?.id) {
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
                skeletonLoading.visibility = View.GONE
                return@launch
            }
            attendees = load.data
            swipeRefresh.isRefreshing = false
            progressBar.visibility = View.GONE
            render(load)
        }
    }

    private fun bindEventHeader() {
        val event = selectedEvent ?: return
        val dateLine = organizerEventDateLine(event.shortDate, event.title, event.venue)
        txtEventSelectorDate.text = if (dateLine.isBlank()) event.title else "${event.title} · $dateLine"
    }

    private fun showNoEventEmptyState() {
        skeletonLoading.visibility = View.GONE
        progressBar.visibility = View.GONE
        swipeRefresh.isEnabled = false
        currentEventLabel.visibility = View.GONE
        cardAttendeeStats.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        emptyStateTitle.text = "No event selected"
        emptyStateSub.text = "Select an event from the dropdown above to view attendees."
    }

    private fun showNoEventsAvailableState() {
        skeletonLoading.visibility = View.GONE
        progressBar.visibility = View.GONE
        swipeRefresh.isEnabled = false
        currentEventLabel.visibility = View.GONE
        txtEventSelectorDate.visibility = View.GONE
        cardAttendeeStats.visibility = View.GONE
        eventSelectorHost.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        emptyStateTitle.text = "No Events Available"
        emptyStateSub.text = "Create an event in the Events tab to start managing attendees."
    }

    private fun restoreNormalUi() {
        currentEventLabel.visibility = View.VISIBLE
        cardAttendeeStats.visibility = View.VISIBLE
        swipeRefresh.isEnabled = true
        emptyStateLayout.visibility = View.GONE
        txtEventSelectorDate.visibility = View.VISIBLE
    }

    private fun render(load: OrganizerMvpLoad<List<OrganizerMvpAttendee>>) {
        skeletonLoading.visibility = View.GONE
        val checkedIn = attendees.count { it.statusBucket().equals("Checked In", ignoreCase = true) }
        val noShow = attendees.count { it.statusBucket().equals("No Show", ignoreCase = true) }

        txtTotal.text = attendees.size.toString()
        txtCheckedIn.text = checkedIn.toString()
        txtNoShow.text = noShow.toString()

        adapter.submitItems(attendees)
        emptyStateLayout.visibility = if (attendees.isEmpty()) View.VISIBLE else View.GONE
        when {
            load.source == OrganizerMvpDataSource.ERROR -> {
                emptyStateTitle.text = "Unable to load attendees"
                emptyStateSub.text = load.message ?: "Please try again later."
            }
            attendees.isEmpty() -> {
                emptyStateTitle.text = "No attendees registered yet"
                emptyStateSub.text = "Attendees will appear here once they register for the event."
            }
        }
    }

    private fun openDetails(attendee: OrganizerMvpAttendee) {
        val event = selectedEvent ?: return
        startActivity(
            Intent(this, AttendeeDetailsActivity::class.java)
                .putExtra(EXTRA_EVENT_ID, event.id)
                .putExtra(EXTRA_EVENT_TITLE, event.title)
                .putExtra(SearchAttendeesActivity.EXTRA_ATTENDEE_ID, attendee.id)
        )
    }
}
