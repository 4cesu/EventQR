package com.thedavelopers.eventqr.features.staff

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.AccountRole
import com.thedavelopers.eventqr.core.session.SessionManager
import com.thedavelopers.eventqr.core.util.RoleMapper
import com.thedavelopers.eventqr.features.staff.model.dto.StaffAssignedEventResponse
import com.thedavelopers.eventqr.features.staff.scanner.ScannerActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

open class StaffAssignedEventsActivity : AppCompatActivity() {
    private lateinit var repository: StaffRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StaffAssignedEventAdapter
    private lateinit var emptyState: android.widget.TextView
    private lateinit var skeletonLoading: View
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        if (RoleMapper.normalizeRole(sessionManager.getUserRole()) != AccountRole.STAFF.name) {
            Toast.makeText(this, "Access Denied: Staff only", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_staff_assigned_events)
        repository = StaffRepository(this)

        recyclerView = findViewById(R.id.recyclerAssignedEvents)
        emptyState = findViewById(R.id.txtAssignedEventsEmpty)
        skeletonLoading = findViewById(R.id.skeletonLoading)
        swipeRefresh = findViewById(R.id.swipeRefreshAssignedEvents)
        swipeRefresh.setColorSchemeResources(R.color.eventqr_purple)
        swipeRefresh.setOnRefreshListener { loadEvents(showLoading = false) }

        adapter = StaffAssignedEventAdapter(
            onScanClick = { event -> openScanner(event) },
            onAttendeesClick = { event -> openAttendees(event) },
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupBottomNav()
        loadEvents()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        loadEvents()
    }

    private fun setupBottomNav() {
        findViewById<View>(R.id.navDashboard)?.setOnClickListener {
            startActivity(Intent(this, StaffDashboardActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navScanner)?.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navLogs)?.setOnClickListener {
            startActivity(Intent(this, StaffTransactionsActivity::class.java))
            finish()
        }
    }

    private fun loadEvents(showLoading: Boolean = true) {
        if (showLoading && !swipeRefresh.isRefreshing) {
            skeletonLoading.visibility = View.VISIBLE
        }
        MainScope().launch {
            when (val result = repository.getEvents()) {
                is NetworkResult.Success -> renderEvents(result.data)
                is NetworkResult.Error -> {
                    adapter.submitItems(emptyList())
                    emptyState.text = result.message
                    emptyState.visibility = View.VISIBLE
                    skeletonLoading.visibility = View.GONE
                    Toast.makeText(this@StaffAssignedEventsActivity, result.message, Toast.LENGTH_SHORT).show()
                }
                NetworkResult.Loading -> Unit
            }
            swipeRefresh.isRefreshing = false
        }
    }

    private fun renderEvents(items: List<StaffAssignedEventResponse>) {
        skeletonLoading.visibility = View.GONE
        emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        adapter.submitItems(items.sortedBy { it.eventStartAt })
    }

    private fun openScanner(event: StaffAssignedEventResponse) {
        startActivity(Intent(this, ScannerActivity::class.java).apply {
            putExtra(StaffScreenExtras.EXTRA_EVENT_ID, event.eventId.toString())
        })
    }

    private fun openAttendees(event: StaffAssignedEventResponse) {
        startActivity(Intent(this, EventRegistrationsActivity::class.java).apply {
            putExtra(StaffScreenExtras.EXTRA_EVENT_ID, event.eventId.toString())
        })
    }
}
