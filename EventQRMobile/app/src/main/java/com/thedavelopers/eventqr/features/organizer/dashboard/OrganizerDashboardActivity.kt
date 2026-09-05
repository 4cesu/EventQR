package com.thedavelopers.eventqr.features.organizer.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.session.SessionManager
import com.thedavelopers.eventqr.core.util.PortalSwitcher
import com.thedavelopers.eventqr.core.util.RoleMapper
import com.thedavelopers.eventqr.features.organizer.*
import com.thedavelopers.eventqr.features.organizer.model.dto.OrganizerDashboardDto
import com.thedavelopers.eventqr.features.organizer.notifications.NotificationManagementActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.min
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

open class OrganizerDashboardActivity : AppCompatActivity() {
    private lateinit var repository: OrganizerRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var skeletonLoading: View
    private var isSwipeRefreshing = false
    private val organizerZone: ZoneId = ZoneId.of("Asia/Manila")
    private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d", Locale.ENGLISH)
    private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organizer_dashboard)
        repository = OrganizerRepository(this)
        sessionManager = SessionManager(this)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshDashboard)
        skeletonLoading = findViewById(R.id.skeletonLoading)
        swipeRefreshLayout.setColorSchemeResources(R.color.eventqr_purple)
        swipeRefreshLayout.setOnRefreshListener {
            isSwipeRefreshing = true
            loadDashboard()
        }
        setupNavigation()
        loadDashboard()
    }

    private fun setupNavigation() {
        setupOrganizerNotificationBell()

        findViewById<View>(R.id.navDashboard).setOnClickListener {
            // Stay here
        }
        findViewById<View>(R.id.navEvents).setOnClickListener {
            openOrganizerPage(ManageEventsActivity::class.java, selectedEventId().takeIf { it.isNotBlank() })
        }
        findViewById<View>(R.id.navAttendees).setOnClickListener {
            openOrganizerPage(
                com.thedavelopers.eventqr.features.organizer.attendees.AttendeeManagementActivity::class.java,
                selectedEventId().takeIf { it.isNotBlank() },
            )
        }
        findViewById<View>(R.id.navReports).setOnClickListener {
            openOrganizerPage(
                com.thedavelopers.eventqr.features.organizer.reports.EventReportsActivity::class.java,
                selectedEventId().takeIf { it.isNotBlank() },
            )
        }

        findViewById<View>(R.id.btnManageMyEvents).setOnClickListener {
            openOrganizerPage(ManageEventsActivity::class.java, selectedEventId().takeIf { it.isNotBlank() })
        }
        findViewById<View>(R.id.btnManageAttendees).setOnClickListener {
            openOrganizerPage(
                com.thedavelopers.eventqr.features.organizer.attendees.AttendeeManagementActivity::class.java,
                selectedEventId().takeIf { it.isNotBlank() },
            )
        }
        findViewById<View>(R.id.btnManageReports).setOnClickListener {
            openOrganizerPage(
                com.thedavelopers.eventqr.features.organizer.reports.EventReportsActivity::class.java,
                selectedEventId().takeIf { it.isNotBlank() },
            )
        }
        findViewById<View>(R.id.btnManageRewards).setOnClickListener {
            openOrganizerPage(
                com.thedavelopers.eventqr.features.organizer.rewards.ManageRewardsActivity::class.java,
                selectedEventId().takeIf { it.isNotBlank() },
            )
        }
        findViewById<View>(R.id.btnSeeAllEvents).setOnClickListener {
            openOrganizerPage(ManageEventsActivity::class.java, selectedEventId().takeIf { it.isNotBlank() })
        }
        findViewById<View>(R.id.btnDashboardRetry).setOnClickListener {
            loadDashboard()
        }

        setupPortalSwitcher()
    }

    private fun setupOrganizerNotificationBell() {
        val contentRoot = findViewById<ViewGroup>(android.R.id.content)
        val appRoot = contentRoot.getChildAt(0) as? LinearLayout ?: return
        val header = appRoot.getChildAt(0) as? RelativeLayout ?: return
        if (header.findViewWithTag<View>("organizer_notification_bell") != null) return

        val headerContent = header.getChildAt(0) as? LinearLayout
        val headerParams = headerContent?.layoutParams as? RelativeLayout.LayoutParams
        if (headerContent != null && headerParams != null) {
            headerParams.marginEnd = dp(56)
            headerContent.layoutParams = headerParams
        }

        val bellContainer = FrameLayout(this).apply {
            tag = "organizer_notification_bell"
            setBackgroundResource(R.drawable.bg_header_icon_circle)
            isClickable = true
            isFocusable = true
            contentDescription = "Notifications"
            setOnClickListener {
                startActivity(Intent(this@OrganizerDashboardActivity, NotificationManagementActivity::class.java))
            }
            layoutParams = RelativeLayout.LayoutParams(dp(40), dp(40)).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        bellContainer.addView(ImageView(this).apply {
            setImageResource(R.drawable.notification_bell)
            setColorFilter(Color.WHITE)
            contentDescription = "Notifications"
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22), android.view.Gravity.CENTER)
        })

        bellContainer.addView(View(this).apply {
            visibility = View.GONE
            setBackgroundResource(R.drawable.bg_red_dot)
            layoutParams = FrameLayout.LayoutParams(dp(8), dp(8), android.view.Gravity.TOP or android.view.Gravity.END).apply {
                topMargin = dp(5)
                marginEnd = dp(5)
            }
        })

        header.addView(bellContainer)
    }

    private fun setupPortalSwitcher() {
        val role = sessionManager.getUserRole() ?: return
        val normalizedRole = RoleMapper.normalizeRole(role)
        val allowedPortals = PortalSwitcher.portalsForRole(normalizedRole)

        val chip = findViewById<View>(R.id.portalSwitcherChip)
        val dot = findViewById<View>(R.id.txtHeaderSubtitleDot)
        chip.visibility = if (allowedPortals.size > 1) View.VISIBLE else View.GONE
        dot.visibility = if (allowedPortals.size > 1) View.VISIBLE else View.GONE
        chip.setOnClickListener(null)

        if (allowedPortals.size > 1) {
            chip.setOnClickListener {
                showPortalSwitcher(allowedPortals)
            }
        }
    }

    private fun showPortalSwitcher(portals: List<String>) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_portal_switcher, null)
        
        val container = view.findViewById<LinearLayout>(R.id.portalOptionsContainer)
        portals.forEach { portal ->
            val portalView = layoutInflater.inflate(R.layout.item_portal_option, container, false)
            portalView.findViewById<TextView>(R.id.txtPortalName).text = portal
            
            val icon = portalView.findViewById<android.widget.ImageView>(R.id.imgPortalIcon)
            val subtitle = portalView.findViewById<TextView>(R.id.txtPortalSubtitle)
            icon.setImageResource(PortalSwitcher.iconRes(portal))
            subtitle.text = PortalSwitcher.subtitle(portal)

            if (portal == PortalSwitcher.PORTAL_ORGANIZER) {
                portalView.findViewById<View>(R.id.currentPortalBadge).visibility = View.VISIBLE
            }

            portalView.setOnClickListener {
                dialog.dismiss()
                switchToPortal(portal)
            }
            container.addView(portalView)
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun switchToPortal(portal: String) {
        when(portal) {
            PortalSwitcher.PORTAL_ATTENDEE -> {
                startActivity(Intent(this, com.thedavelopers.eventqr.features.dashboard.DashboardActivity::class.java))
                finish()
            }
            PortalSwitcher.PORTAL_STAFF -> {
                startActivity(Intent(this, com.thedavelopers.eventqr.features.staff.StaffDashboardActivity::class.java))
                finish()
            }
            PortalSwitcher.PORTAL_ORGANIZER -> {
                // Already here
            }
            PortalSwitcher.PORTAL_ADMIN -> {
                startActivity(Intent(this, com.thedavelopers.eventqr.features.admin.dashboard.AdminDashboardActivity::class.java))
                finish()
            }
            PortalSwitcher.PORTAL_SUPER_ADMIN -> {
                startActivity(Intent(this, com.thedavelopers.eventqr.features.admin.dashboard.AdminDashboardActivity::class.java))
                finish()
            }
        }
    }

    private fun loadDashboard() {
        if (!isSwipeRefreshing) {
            skeletonLoading.visibility = View.VISIBLE
        } else {
            skeletonLoading.visibility = View.GONE
        }
        findViewById<View>(R.id.layoutDashboardError).visibility = View.GONE
        MainScope().launch {
            try {
                val dashboard = repository.loadDashboardForMvp()
                val load = repository.loadEventsForMvp()
                renderDashboard(load, dashboard)
            } finally {
                stopSwipeRefresh()
            }
        }
    }

    private fun renderDashboard(
        load: OrganizerMvpLoad<List<OrganizerMvpEvent>>,
        dashboard: OrganizerMvpLoad<OrganizerDashboardDto?>? = null,
    ) {
        findViewById<ProgressBar>(R.id.progressDashboardLoading).visibility = View.GONE
        skeletonLoading.visibility = View.GONE
        val dashboardData = dashboard?.data
        val name = dashboardData?.organizerName.orEmpty().ifBlank { sessionManager.getFullName().orEmpty().ifBlank { "Organizer" } }

        findViewById<TextView>(R.id.txtHeaderTitle).text = PortalSwitcher.PORTAL_ORGANIZER
        findViewById<TextView>(R.id.txtHeaderSubtitle).text = name

        val events = load.data.approvedOnly()
        val activeEvents = events.filter { it.lifecycleStatus() != "Completed" }
        val selected = repository.resolveSelectedEvent(events, selectedEventId())
        val totalAttendees = dashboardData?.totalAttendees ?: events.sumOf { it.registeredCount }
        val totalTransactions = dashboardData?.totalTransactions ?: events.sumOf { it.totalTransactions }
        val totalRewards = events.sumOf { it.rewardRedemptions }
        val totalEvents = dashboardData?.totalEvents ?: events.size

        findViewById<TextView>(R.id.txtStatTotalEvents).text = formatCount(totalEvents)
        findViewById<TextView>(R.id.txtStatTotalAttendees).text = formatCount(totalAttendees)
        findViewById<TextView>(R.id.txtStatScansToday).text = formatCount(totalTransactions)
        findViewById<TextView>(R.id.txtStatRewardsGiven).text = formatCount(totalRewards)

        val activeEventsContainer = findViewById<LinearLayout>(R.id.activeEventsContainer)
        val emptyEvents = findViewById<TextView>(R.id.txtActiveEventsEmpty)
        activeEventsContainer.removeAllViews()

        val hasError = load.source == OrganizerMvpDataSource.ERROR
        findViewById<View>(R.id.layoutDashboardError).visibility = if (hasError && events.isEmpty()) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.txtDashboardError).text = load.message ?: "Organizer events could not be loaded."

        if (activeEvents.isEmpty()) {
            emptyEvents.visibility = View.VISIBLE
        } else {
            emptyEvents.visibility = View.GONE
            activeEvents.take(3).forEach { event ->
                activeEventsContainer.addView(dashboardEventCard(event) {
                    val target = selected?.takeIf { it.id == event.id } ?: event
                    openOrganizerPage(EventManagementHubActivity::class.java, target.id, target.title)
                })
            }
        }
    }

    private fun dashboardEventCard(
        event: OrganizerMvpEvent,
        onClick: () -> Unit,
    ): View {
        val parsedStart = parseEventStartDateTime(event)
        val parsedDate = parsedStart?.toLocalDate() ?: parseEventDateOnly(event)
        val day = parsedDate?.format(dayFormatter) ?: "--"
        val month = parsedDate?.format(monthFormatter)?.uppercase(Locale.ENGLISH) ?: "---"
        val time = parsedStart?.format(timeFormatter) ?: "-"
        val location = event.venue.takeIf { it.isNotBlank() && it != "Venue not set" } ?: "Location not set"

        val capacity = event.capacity.coerceAtLeast(1)
        val count = event.currentAttendeeCount.coerceAtLeast(0)
        val percent = if (capacity > 0) min((count.toFloat() / capacity.toFloat() * 100f).toInt(), 100) else 0

        return com.thedavelopers.eventqr.features.events.EventCardBinder.inflate(
            context = this,
            parent = null,
            title = event.title,
            status = event.lifecycleStatus(),
            day = day,
            month = month,
            time = time,
            location = location,
            count = count,
            capacity = capacity,
            percent = percent,
            onClick = { onClick() },
        )
    }

    private fun parseEventStartDateTime(event: OrganizerMvpEvent): LocalDateTime? {
        val candidates = listOfNotNull(event.dateTime, event.shortDate)
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "-" }

        candidates.forEach { raw ->
            val firstPart = raw.substringBefore(" - ").trim()
            parseDateTimeValue(firstPart)?.let { return it }
            parseDateTimeValue(raw)?.let { return it }
        }
        return null
    }

    private fun parseEventDateOnly(event: OrganizerMvpEvent): LocalDate? {
        val candidates = listOfNotNull(event.shortDate, event.dateTime)
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "-" }

        candidates.forEach { raw ->
            val firstPart = raw.substringBefore(" - ").trim()
            parseDateValue(firstPart)?.let { return it }
            parseDateValue(raw)?.let { return it }
        }
        return null
    }

    private fun parseDateTimeValue(value: String): LocalDateTime? {
        val normalized = value.replace("•", "").replace("  ", " ").trim()
        return runCatching { Instant.parse(normalized).atZone(organizerZone).toLocalDateTime() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(normalized).atZoneSameInstant(organizerZone).toLocalDateTime() }.getOrNull()
            ?: runCatching { ZonedDateTime.parse(normalized).withZoneSameInstant(organizerZone).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH)) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH)) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.ENGLISH)) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a", Locale.ENGLISH)) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) }.getOrNull()
    }

    private fun parseDateValue(value: String): LocalDate? {
        val normalized = value.replace("•", "").replace("  ", " ").trim()
        return runCatching { LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
            ?: runCatching { LocalDate.parse(normalized, DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)) }.getOrNull()
            ?: runCatching { LocalDate.parse(normalized, DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)) }.getOrNull()
            ?: parseDateTimeValue(normalized)?.toLocalDate()
    }

    private fun stopSwipeRefresh() {
        if (isSwipeRefreshing) {
            swipeRefreshLayout.isRefreshing = false
            isSwipeRefreshing = false
        }
    }
}
