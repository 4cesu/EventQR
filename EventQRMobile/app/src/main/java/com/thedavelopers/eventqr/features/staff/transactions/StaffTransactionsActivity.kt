package com.thedavelopers.eventqr.features.staff

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
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
import com.thedavelopers.eventqr.features.transactions.TransactionLogAdapter
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

open class StaffTransactionsActivity : AppCompatActivity(), StaffTransactionsContract.View {

    private lateinit var repository: StaffRepository
    private lateinit var adapter: TransactionLogAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var skeletonLoading: View
    private lateinit var eventCard: LinearLayout
    private lateinit var eventTitleView: TextView
    private lateinit var eventDateView: TextView
    private lateinit var eventChevron: TextView
    private lateinit var purposeCard: LinearLayout
    private lateinit var purposeTitleView: TextView
    private lateinit var purposeChevron: TextView
    private lateinit var txtTotalScans: TextView
    private lateinit var txtSuccessfulScans: TextView
    private lateinit var txtRejectedScans: TextView
    private lateinit var txtEmptyState: TextView
    private lateinit var recyclerView: RecyclerView

    private var assignedEvents: List<StaffAssignedEventResponse> = emptyList()
    private var selectedEventId: String? = null
    private var selectedPurposeId: String? = null
    private var purposeOptions: List<PurposeOption> = emptyList()
    private var eventPopup: PopupWindow? = null
    private var purposePopup: PopupWindow? = null
    private var isEventPopupOpen = false
    private var isPurposePopupOpen = false

    private val manilaZone: ZoneId = ZoneId.of("Asia/Manila")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        if (RoleMapper.normalizeRole(sessionManager.getUserRole()) != AccountRole.STAFF.name) {
            Toast.makeText(this, "Access Denied: Staff only", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_staff_transaction_logs)

        repository = StaffRepository(this)
        adapter = TransactionLogAdapter()

        eventCard = findViewById(R.id.cardStaffTransactionsEvent)
        eventTitleView = findViewById(R.id.txtStaffTransactionsEventTitle)
        eventDateView = findViewById(R.id.txtStaffTransactionsEventDate)
        eventChevron = findViewById(R.id.txtStaffTransactionsEventChevron)
        purposeCard = findViewById(R.id.cardStaffTransactionsPurpose)
        purposeTitleView = findViewById(R.id.txtStaffTransactionsPurposeTitle)
        purposeChevron = findViewById(R.id.txtStaffTransactionsPurposeChevron)
        txtTotalScans = findViewById(R.id.txtTotalScans)
        txtSuccessfulScans = findViewById(R.id.txtSuccessfulScans)
        txtRejectedScans = findViewById(R.id.txtRejectedScans)
        txtEmptyState = findViewById(R.id.txtStaffTransactionsEmptyState)
        recyclerView = findViewById(R.id.recyclerStaffTransactions)

        swipeRefresh = findViewById(R.id.swipeRefreshStaffTransactions)
        swipeRefresh.setColorSchemeResources(R.color.eventqr_purple)
        swipeRefresh.setOnRefreshListener { loadTransactions() }

        skeletonLoading = findViewById(R.id.skeletonLoading)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StaffTransactionsActivity)
            adapter = this@StaffTransactionsActivity.adapter
        }

        eventCard.visibility = View.VISIBLE
        purposeCard.visibility = View.VISIBLE

        eventCard.setOnClickListener { toggleEventPopup() }
        purposeCard.setOnClickListener { togglePurposePopup() }

        setupBottomNav()
        loadAssignedEventsAndTransactions()
    }

    private fun loadAssignedEventsAndTransactions() {
        MainScope().launch {
            showLoading(true)
            when (val eventsResult = repository.getEvents()) {
                is NetworkResult.Success -> {
                    assignedEvents = eventsResult.data.filter { it.canScan && it.status.name != "ENDED" }
                    selectedEventId = intent.getStringExtra(StaffScreenExtras.EXTRA_EVENT_ID)
                        ?.takeIf { extra -> assignedEvents.any { it.eventId.toString() == extra } }
                    if (assignedEvents.isEmpty()) {
                        renderTransactions(emptyList())
                        showMessage("No assigned events found")
                        showLoading(false)
                        return@launch
                    }
                    bindEventHeader()
                    loadTransactions()
                }
                is NetworkResult.Error -> {
                    renderTransactions(emptyList())
                    showMessage(eventsResult.message)
                    showLoading(false)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun loadTransactions() {
        MainScope().launch {
            showLoading(true)
            when (val result = repository.getMyTransactions(selectedEventId, selectedPurposeId)) {
                is NetworkResult.Success -> {
                    val items = result.data
                    purposeOptions = buildPurposeOptions(items)
                    if (selectedPurposeId != null && purposeOptions.none { it.id == selectedPurposeId }) {
                        selectedPurposeId = null
                    }
                    bindPurposeHeader()
                    renderTransactions(items)
                }
                is NetworkResult.Error -> {
                    renderTransactions(emptyList())
                    showMessage(result.message)
                }
                NetworkResult.Loading -> Unit
            }
            showLoading(false)
        }
    }

    private fun buildPurposeOptions(items: List<TransactionResponse>): List<PurposeOption> {
        return items
            .mapNotNull { tx ->
                val fallbackName = tx.transactionType.name
                    .lowercase(Locale.US)
                    .split('_')
                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase(Locale.US) } }
                PurposeOption(
                    id = tx.scanPurposeId.toString(),
                    label = tx.scanPurposeName?.takeIf { it.isNotBlank() } ?: fallbackName,
                )
            }
            .distinctBy { it.id }
            .sortedBy { it.label.lowercase(Locale.US) }
    }

    private fun bindEventHeader() {
        val selected = assignedEvents.firstOrNull { it.eventId.toString() == selectedEventId }
        eventTitleView.text = selected?.title ?: "All Events"
        eventDateView.text = selected?.eventStartAt
            ?.atZone(manilaZone)
            ?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH))
            .orEmpty()
    }

    private fun bindPurposeHeader() {
        val selected = purposeOptions.firstOrNull { it.id == selectedPurposeId }
        purposeTitleView.text = selected?.label ?: "All Purposes"
    }

    private fun toggleEventPopup() {
        if (isEventPopupOpen) {
            closeEventPopup()
            return
        }
        if (assignedEvents.isEmpty()) return
        closePurposePopup()
        eventPopup = PopupWindow(
            buildEventDropdownView(),
            eventCard.width.takeIf { it > 0 } ?: ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            isOutsideTouchable = true
            elevation = dp(8).toFloat()
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener {
                isEventPopupOpen = false
                setChevron(eventChevron, false)
            }
        }
        isEventPopupOpen = true
        setChevron(eventChevron, true)
        eventPopup?.showAsDropDown(eventCard, 0, 0)
    }

    private fun closeEventPopup() {
        eventPopup?.dismiss()
        isEventPopupOpen = false
        setChevron(eventChevron, false)
    }

    private fun togglePurposePopup() {
        if (isPurposePopupOpen) {
            closePurposePopup()
            return
        }
        if (purposeOptions.isEmpty()) {
            showMessage("No scan purposes in your transaction logs yet.")
            return
        }
        closeEventPopup()
        purposePopup = PopupWindow(
            buildPurposeDropdownView(),
            purposeCard.width.takeIf { it > 0 } ?: ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            isOutsideTouchable = true
            elevation = dp(8).toFloat()
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener {
                isPurposePopupOpen = false
                setChevron(purposeChevron, false)
            }
        }
        isPurposePopupOpen = true
        setChevron(purposeChevron, true)
        purposePopup?.showAsDropDown(purposeCard, 0, 0)
    }

    private fun closePurposePopup() {
        purposePopup?.dismiss()
        isPurposePopupOpen = false
        setChevron(purposeChevron, false)
    }

    private fun buildEventDropdownView(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.bg_card)
        val allSelected = selectedEventId == null
        addView(buildFilterOptionRow("All Events", allSelected) {
            selectedEventId = null
            bindEventHeader()
            closeEventPopup()
            loadTransactions()
        }.apply { if (allSelected) setBackgroundColor(Color.parseColor("#EEF2FF")) })

        assignedEvents.forEach { event ->
            val isSelected = selectedEventId == event.eventId.toString()
            addView(buildFilterOptionRow(event.title, isSelected) {
                selectedEventId = event.eventId.toString()
                selectedPurposeId = null
                bindEventHeader()
                bindPurposeHeader()
                closeEventPopup()
                loadTransactions()
            }.apply { if (isSelected) setBackgroundColor(Color.parseColor("#EEF2FF")) })
        }
    }

    private fun buildPurposeDropdownView(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.bg_card)
        val allSelected = selectedPurposeId == null
        addView(buildFilterOptionRow("All Purposes", allSelected) {
            selectedPurposeId = null
            bindPurposeHeader()
            closePurposePopup()
            loadTransactions()
        }.apply { if (allSelected) setBackgroundColor(Color.parseColor("#EEF2FF")) })

        purposeOptions.forEach { purpose ->
            val isSelected = selectedPurposeId == purpose.id
            addView(buildFilterOptionRow(purpose.label, isSelected) {
                selectedPurposeId = purpose.id
                bindPurposeHeader()
                closePurposePopup()
                loadTransactions()
            }.apply { if (isSelected) setBackgroundColor(Color.parseColor("#EEF2FF")) })
        }
    }

    private fun buildFilterOptionRow(label: String, isSelected: Boolean, onClick: () -> Unit): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            if (!isSelected) setBackgroundColor(Color.WHITE)
            setOnClickListener { onClick() }
            addView(TextView(this@StaffTransactionsActivity).apply {
                text = label
                setTextColor(if (isSelected) 0xFF4F46E5.toInt() else 0xFF111827.toInt())
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
        }

    private fun setChevron(view: TextView, open: Boolean) {
        view.includeFontPadding = false
        view.translationY = -dp(1).toFloat()
        view.text = if (open) "\u25B4" else "\u25BE"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun setupBottomNav() {
        configureStaffBottomNav(StaffBottomNavItem.LOGS, selectedEventId)
    }

    override fun onDestroy() {
        eventPopup?.dismiss()
        purposePopup?.dismiss()
        super.onDestroy()
    }

    override fun renderTransactions(items: List<TransactionResponse>) {
        swipeRefresh.isRefreshing = false
        skeletonLoading.visibility = View.GONE
        adapter.submitItems(items.sortedByDescending { it.scannedAt ?: Instant.EPOCH })
        txtTotalScans.text = items.size.toString()
        txtSuccessfulScans.text = items.count { it.transactionResult.name == "APPROVED" || it.transactionResult.name == "SUCCESS" }.toString()
        txtRejectedScans.text = items.count { it.transactionResult.name != "APPROVED" && it.transactionResult.name != "SUCCESS" }.toString()
        txtEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun showMessage(message: String) {
        swipeRefresh.isRefreshing = false
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(isLoading: Boolean) {
        if (isLoading && !swipeRefresh.isRefreshing) {
            skeletonLoading.visibility = View.VISIBLE
        }
        if (!isLoading) {
            swipeRefresh.isRefreshing = false
        }
    }

    private data class PurposeOption(val id: String, val label: String)
}