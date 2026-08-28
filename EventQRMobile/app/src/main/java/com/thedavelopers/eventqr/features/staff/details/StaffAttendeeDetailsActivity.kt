package com.thedavelopers.eventqr.features.staff.details

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.AccountRole
import com.thedavelopers.eventqr.core.session.SessionManager
import com.thedavelopers.eventqr.core.util.RoleMapper
import com.thedavelopers.eventqr.features.idprinting.AndroidIdPrinter
import com.thedavelopers.eventqr.features.registrations.RegistrationNumberFormatter
import com.thedavelopers.eventqr.features.registrations.RegistrationStatusBadgeStyler
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse
import com.thedavelopers.eventqr.features.staff.StaffRepository
import com.thedavelopers.eventqr.features.staff.StaffScreenExtras
import com.thedavelopers.eventqr.features.staff.orUnknown
import com.thedavelopers.eventqr.features.staff.scanner.ScannerActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

open class StaffAttendeeDetailsActivity : AppCompatActivity() {
    private lateinit var repository: StaffRepository
    private lateinit var sessionManager: SessionManager
    private var eventId: String = ""
    private var attendeeId: String = ""
    private var registrationId: String = ""
    private var qrCredentialId: String = ""
    private var hasPrintedId: Boolean = false
    private var cachedAttendeeName: String = ""
    private var cachedEventName: String = ""
    private var cachedRegistrationNumber: Int? = null
    private var cachedRole: String = ""
    private var cachedEventDate: String = ""

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        .withZone(ZoneId.of("Asia/Manila"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (RoleMapper.normalizeRole(sessionManager.getUserRole()) != AccountRole.STAFF.name) {
            Toast.makeText(this, "Access Denied: Staff only", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_staff_attendee_details)
        repository = StaffRepository(this)
        eventId = intent.getStringExtra(StaffScreenExtras.EXTRA_EVENT_ID).orEmpty()
        attendeeId = intent.getStringExtra(StaffScreenExtras.EXTRA_ATTENDEE_ID).orEmpty()
        registrationId = intent.getStringExtra(StaffScreenExtras.EXTRA_REGISTRATION_ID).orEmpty()
        qrCredentialId = intent.getStringExtra(StaffScreenExtras.EXTRA_QR_CREDENTIAL_ID).orEmpty()

        findViewById<View>(R.id.btnBackToTransactionResult).setOnClickListener { finish() }
        findViewById<View>(R.id.btnPrintOrReprintId).setOnClickListener { printId() }
        findViewById<View>(R.id.btnScanAgain).setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java).apply {
                putExtra(StaffScreenExtras.EXTRA_EVENT_ID, eventId)
            })
        }

        loadDetails()
    }

    private fun loadDetails() {
        if (eventId.isBlank() || attendeeId.isBlank()) {
            Toast.makeText(this, "Missing attendee context", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ProgressBar>(R.id.progressAttendeeDetails).visibility = View.VISIBLE
        MainScope().launch {
            when (val attendeeResult = repository.getAttendeeByEvent(eventId, attendeeId)) {
                is NetworkResult.Success -> {
                    renderRegistration(attendeeResult.data)
                    loadTransactions()
                    loadPrintLogs()
                }
                is NetworkResult.Error -> Toast.makeText(this@StaffAttendeeDetailsActivity, attendeeResult.message, Toast.LENGTH_SHORT).show()
                NetworkResult.Loading -> Unit
            }
            findViewById<ProgressBar>(R.id.progressAttendeeDetails).visibility = View.GONE
        }
    }

    private fun renderRegistration(item: RegistrationResponse) {
        registrationId = item.registrationId.toString()
        qrCredentialId = item.qrCredentialId?.toString().orEmpty()
        cachedAttendeeName = item.attendeeName.orUnknown()
        cachedEventName = item.eventTitle.orUnknown("Assigned event")
        cachedRegistrationNumber = item.registrationNumber
        cachedRole = item.attendeeRole.orEmpty()
        cachedEventDate = item.eventStartAt?.let { formatEventDate(it) }.orEmpty()

        val attendeeName = item.attendeeName.orUnknown()
        findViewById<TextView>(R.id.txtDetailAvatar).text = attendeeName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"
        findViewById<TextView>(R.id.txtDetailAttendeeName).text = attendeeName
        findViewById<TextView>(R.id.txtDetailAttendeeEmail).text = item.attendeeEmail.orUnknown()
        findViewById<TextView>(R.id.txtDetailAttendeePhone).text = item.attendeePhoneNumber?.takeIf { it.isNotBlank() } ?: "No phone number"
        findViewById<TextView>(R.id.txtDetailEventName).text = item.eventTitle.orUnknown("Assigned event")
        findViewById<TextView>(R.id.txtDetailRegistrationId).text =
            RegistrationNumberFormatter.format(item.registrationNumber) ?: shortRegistrationId(item.registrationId)
        RegistrationStatusBadgeStyler.bind(findViewById(R.id.txtDetailRegistrationStatus), item.status)
        findViewById<TextView>(R.id.txtDetailCheckInTime).text = formatTime(item.enteredAt ?: item.attendedAt)
        findViewById<TextView>(R.id.txtDetailPointsBalance).text = "${item.pointsEarned} pts"
        findViewById<TextView>(R.id.txtDetailTransactionCount).text = "0"

        findViewById<TextView>(R.id.txtDetailQrStatus).text = if (item.qrCredentialId == null) "QR Credential: Pending" else "QR Credential: Issued"
        findViewById<TextView>(R.id.txtDetailEntryStatus).text = RegistrationStatusBadgeStyler.displayLabel(item.status)
        findViewById<TextView>(R.id.txtDetailAttendanceStatus).text = if (item.attendedAt != null || item.enteredAt != null) "Checked In" else "Registered"
        findViewById<TextView>(R.id.txtDetailExitStatus).text = if (item.exitedAt != null) "Exited" else "Not exited"
        findViewById<TextView>(R.id.txtDetailRegistrationDate).text = item.registeredAt?.let { "Registered: ${formatTime(it)}" } ?: "Registered: Unknown"
        findViewById<View>(R.id.btnPrintOrReprintId).visibility = if (qrCredentialId.isBlank()) View.GONE else View.VISIBLE
    }

    private fun loadTransactions() {
        MainScope().launch {
            when (val txResult = repository.getTransactionsByEvent(eventId)) {
                is NetworkResult.Success -> {
                    val count = txResult.data.count { it.attendeeUserId.toString() == attendeeId }
                    findViewById<TextView>(R.id.txtDetailTransactionCount).text = count.toString()
                }
                is NetworkResult.Error -> findViewById<TextView>(R.id.txtDetailTransactionCount).text = "0"
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun loadPrintLogs() {
        if (qrCredentialId.isBlank()) {
            hasPrintedId = false
            findViewById<TextView>(R.id.txtPrintOrReprintIdLabel).text = "Print ID"
            return
        }

        MainScope().launch {
            when (val result = repository.getStaffPrintLogs(eventId)) {
                is NetworkResult.Success -> {
                    hasPrintedId = result.data.any { it.attendeeUserId.toString() == attendeeId }
                    findViewById<TextView>(R.id.txtPrintOrReprintIdLabel).text = if (hasPrintedId) "Reprint ID" else "Print ID"
                }
                is NetworkResult.Error -> Unit
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun printId() {
        if (eventId.isBlank() || attendeeId.isBlank()) {
            Toast.makeText(this, "Attendee context is required for printing", Toast.LENGTH_SHORT).show()
            return
        }

        findViewById<ProgressBar>(R.id.progressAttendeeDetails).visibility = View.VISIBLE
        MainScope().launch {
            val cardData = buildCardData()
            findViewById<ProgressBar>(R.id.progressAttendeeDetails).visibility = View.GONE
            showPrintPreview(cardData)
        }
    }

    private suspend fun buildCardData(): AndroidIdPrinter.CardData {
        val apiService = com.thedavelopers.eventqr.core.api.ApiClient.getService(this@StaffAttendeeDetailsActivity)

        // Fetch template config for visible fields
        var visibleFields = emptyList<String>()
        val configResult = com.thedavelopers.eventqr.core.api.safeApiCall {
            apiService.getIdTemplateConfig(eventId)
        }
        if (configResult is NetworkResult.Success) {
            visibleFields = configResult.data.visibleFields.filterNotNull()
        }

        // Fetch QR credential value for the printed QR code
        var qrValue = ""
        if (qrCredentialId.isNotBlank()) {
            val qrResult = com.thedavelopers.eventqr.core.api.safeApiCall {
                apiService.getQrCredentialById(qrCredentialId)
            }
            if (qrResult is NetworkResult.Success) {
                qrValue = qrResult.data.qrValue
            }
        }

        return AndroidIdPrinter.CardData(
            attendeeName = cachedAttendeeName,
            eventName = cachedEventName,
            registrationNumber = cachedRegistrationNumber,
            role = cachedRole,
            eventDate = cachedEventDate,
            visibleFields = visibleFields,
            qrValue = qrValue,
        )
    }

    /**
     * Print confirmation dialog: shows the full multi-up grid preview plus a
     * 1/2/4/8-up chooser, matching the app's existing confirm-before-print pattern.
     */
    private fun showPrintPreview(cardData: AndroidIdPrinter.CardData) {
        val upCounts = AndroidIdPrinter.UpCount.entries
        var selected = AndroidIdPrinter.UpCount.EIGHT
        val defaultIndex = upCounts.indexOf(selected)

        val previewWidthPx = (resources.displayMetrics.widthPixels * 0.82f).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(4))
        }

        container.addView(TextView(this).apply {
            text = if (hasPrintedId) "Reprint ID" else "Print ID"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF111827.toInt())
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, dp(8))
        })

        val previewImage = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(0, 0, 0, dp(8))
        }

        val gridLabel = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFF111827.toInt())
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(0, dp(8), 0, 0)
        }

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }

        val buttons = upCounts.mapIndexed { index, up ->
            RadioButton(this).apply {
                id = View.generateViewId()
                text = up.label
                textSize = 13f
            }.also { radioGroup.addView(it) }
        }
        radioGroup.check(buttons[defaultIndex].id)

        fun refresh() {
            previewImage.setImageBitmap(AndroidIdPrinter.renderGridPreview(cardData, selected, previewWidthPx))
            gridLabel.text = "${selected.label} · ${selected.copies} cards on one A4 sheet (${selected.cols}×${selected.rows} grid) · cut along the dashed guides"
        }

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selected = upCounts[index]
                refresh()
            }
        }

        container.addView(previewImage)
        container.addView(gridLabel)
        container.addView(radioGroup)

        AlertDialog.Builder(this)
            .setView(container)
            .setPositiveButton("Confirm Print") { _, _ -> submitPrint(cardData, selected) }
            .setNegativeButton("Cancel", null)
            .show()
        refresh()
    }

    private fun submitPrint(cardData: AndroidIdPrinter.CardData, upCount: AndroidIdPrinter.UpCount) {
        findViewById<ProgressBar>(R.id.progressAttendeeDetails).visibility = View.VISIBLE
        MainScope().launch {
            // Batch endpoint creates one ID Print Log entry per attendee entry —
            // here the same attendee is repeated once per card to fill the sheet.
            val result = repository.printIdBatch(
                eventId,
                List(upCount.copies) { UUID.fromString(attendeeId) },
                hasPrintedId,
            )
            when (result) {
                is NetworkResult.Success -> {
                    AndroidIdPrinter.print(
                        this@StaffAttendeeDetailsActivity,
                        "EventQR ID — $cachedAttendeeName",
                        cardData,
                        upCount,
                    )
                    val message = result.data.firstOrNull()?.message ?: "Print request sent"
                    Toast.makeText(this@StaffAttendeeDetailsActivity, message, Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error -> Toast.makeText(this@StaffAttendeeDetailsActivity, result.message, Toast.LENGTH_SHORT).show()
                NetworkResult.Loading -> Unit
            }
            findViewById<ProgressBar>(R.id.progressAttendeeDetails).visibility = View.GONE
            loadPrintLogs()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun formatTime(value: Instant?): String = value?.let { timeFormatter.format(it) } ?: "--"

    private fun formatEventDate(value: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
            .withZone(ZoneId.of("Asia/Manila"))
        return formatter.format(value)
    }

    private fun shortRegistrationId(value: UUID): String = "reg-${value.toString().take(8)}"
}
