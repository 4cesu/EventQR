package com.thedavelopers.eventqr.features.organizer.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.features.events.model.dto.EventRequest
import com.thedavelopers.eventqr.features.organizer.OrganizerRepository
import com.thedavelopers.eventqr.features.organizer.model.dto.OrganizerEventDto
import com.thedavelopers.eventqr.features.organizer.BORDER
import com.thedavelopers.eventqr.features.organizer.ERROR
import com.thedavelopers.eventqr.features.organizer.MUTED
import com.thedavelopers.eventqr.features.organizer.card
import com.thedavelopers.eventqr.features.organizer.dp
import com.thedavelopers.eventqr.features.organizer.intentEventId
import com.thedavelopers.eventqr.features.organizer.intentEventTitle
import com.thedavelopers.eventqr.features.organizer.organizerShell
import com.thedavelopers.eventqr.features.organizer.primaryButton
import com.thedavelopers.eventqr.features.organizer.rounded
import com.thedavelopers.eventqr.features.organizer.showMissingEventScreen
import com.thedavelopers.eventqr.features.organizer.text
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * SDD 3.5 (UC-20) — Manage Approved Event Details.
 *
 * Deviation notes (capstone defense):
 * - Built with the programmatic View toolkit used by every other organizer screen (team
 *   decision), not Compose as the module text implies; no ViewModel — repositories plus
 *   coroutine launches inside activities, matching the rest of this codebase.
 * - Date/time entry reuses the DatePickerDialog + TimePickerDialog pattern from the attendee
 *   event-request form, but deliberately allows PAST dates here (UC-20 locked scope: capstone
 *   demo needs past-dated test events editable).
 */
class EditEventDetailsActivity : AppCompatActivity() {

    private lateinit var repository: OrganizerRepository
    private lateinit var eventId: String
    private lateinit var content: LinearLayout
    private lateinit var statusView: TextView
    private lateinit var saveButton: Button

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var venueInput: EditText
    private lateinit var capacityInput: EditText
    private lateinit var startDateTimeInput: EditText

    private var loadedEvent: OrganizerEventDto? = null
    private var startDateTimeValue: LocalDateTime? = null

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = OrganizerRepository(this)
        eventId = intentEventId() ?: return showMissingEventScreen("Edit Event Details")

        content = organizerShell(
            title = "Edit Event Details",
            subtitle = intentEventTitle()?.takeIf { it.isNotBlank() },
            showBack = true,
        )
        content.addView(buildForm())
        statusView = text("", 13, false).apply { setPadding(dp(4), dp(8), dp(4), 0) }
        content.addView(statusView)
        saveButton = primaryButton("Save Changes") { saveChanges() }.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48),
            ).apply { setMargins(0, dp(12), 0, dp(16)) }
        }
        content.addView(saveButton)

        loadEvent()
    }

    private fun buildForm(): LinearLayout {
        val formCard = card(16)
        formCard.addView(text("Editable details", 15, true, TEXT_COLOR).apply { setPadding(0, 0, 0, dp(4)) })

        titleInput = addInput(formCard, "Title", inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        descriptionInput = addInput(formCard, "Description", inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE, singleLine = false, minLines = 3)
        venueInput = addInput(formCard, "Venue")
        capacityInput = addInput(formCard, "Capacity", inputType = InputType.TYPE_CLASS_NUMBER)
        startDateTimeInput = addInput(formCard, "Event Start Date & Time")
        configureDateTimeField(startDateTimeInput) { selected ->
            startDateTimeValue = selected
            startDateTimeInput.setText(selected.format(displayFormatter))
            startDateTimeInput.error = null
        }

        formCard.addView(
            text("Locked: eventId, organizer, approval status, registration windows, end date, logo and rewards stay unchanged.", 12, false, MUTED).apply {
                setPadding(0, dp(12), 0, 0)
            },
        )
        return formCard
    }

    private fun addInput(
        parent: LinearLayout,
        label: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        singleLine: Boolean = true,
        minLines: Int = 1,
    ): EditText {
        parent.addView(text(label, 14, true, TEXT_COLOR).apply { setPadding(0, dp(12), 0, dp(6)) })
        val editText = EditText(this).apply {
            this.inputType = inputType
            isSingleLine = singleLine
            this.minLines = minLines
            background = rounded(android.graphics.Color.parseColor("#F9FAFB"), 10, BORDER, density = resources.displayMetrics.density)
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }
        parent.addView(editText)
        return editText
    }

    // Same widgets as the attendee request form's date-time field, minus its minDate/past-time
    // rejection: UC-20 locked scope allows editing past-dated events.
    private fun configureDateTimeField(field: EditText, onSelected: (LocalDateTime) -> Unit) {
        field.isFocusable = false
        field.isFocusableInTouchMode = false
        field.isCursorVisible = false
        field.isLongClickable = false
        field.setTextIsSelectable(false)
        field.setOnClickListener {
            val initial = startDateTimeValue ?: LocalDateTime.now(zoneId)
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    val initialTime = if (selectedDate == initial.toLocalDate()) {
                        initial.toLocalTime().withSecond(0).withNano(0)
                    } else {
                        LocalTime.of(9, 0)
                    }
                    TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            onSelected(LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute)))
                        },
                        initialTime.hour,
                        initialTime.minute,
                        false,
                    ).show()
                },
                initial.year,
                initial.monthValue - 1,
                initial.dayOfMonth,
            ).show()
        }
    }

    private fun loadEvent() {
        MainScope().launch {
            when (val result = repository.fetchOrganizerEvent(eventId)) {
                is NetworkResult.Success -> result.data?.let { populate(it) }
                is NetworkResult.Error -> {
                    statusView.text = result.message
                    statusView.setTextColor(ERROR)
                    saveButton.isEnabled = false
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun populate(event: OrganizerEventDto) {
        loadedEvent = event
        titleInput.setText(event.title.orEmpty())
        descriptionInput.setText(event.description.orEmpty())
        venueInput.setText(event.venue.orEmpty())
        capacityInput.setText(event.capacity.coerceAtLeast(0).toString())
        event.eventStartAt?.let { instant ->
            startDateTimeValue = LocalDateTime.ofInstant(instant, zoneId)
            startDateTimeInput.setText(startDateTimeValue!!.format(displayFormatter))
        }
    }

    private fun saveChanges() {
        val title = titleInput.text.toString().trim()
        val capacityValue = capacityInput.text.toString().trim().toIntOrNull()

        titleInput.error = if (title.isBlank()) "Title is required" else null
        if (startDateTimeValue == null) startDateTimeInput.error = "Date is required"
        if (capacityValue == null || capacityValue <= 0) capacityInput.error = "Capacity must be greater than 0"
        if (title.isBlank() || startDateTimeValue == null || capacityValue == null || capacityValue <= 0) return

        val current = loadedEvent ?: run {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }
        // EventRequest.organizerUserId is required by the API; older backends that predate
        // UC-20 don't echo it, so fail softly instead of crashing.
        val organizerId = current.organizerUserId ?: run {
            statusView.text = "Backend does not report this event's organizer; update the app server."
            statusView.setTextColor(ERROR)
            return
        }

        saveButton.isEnabled = false
        statusView.text = ""
        MainScope().launch {
            when (val result = repository.updateOrganizerEvent(eventId, buildRequest(current, organizerId))) {
                is NetworkResult.Success -> {
                    Toast.makeText(this@EditEventDetailsActivity, "Event updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is NetworkResult.Error -> {
                    // Backend messages surface verbatim through safeApiCall: field validation
                    // details on 400, permission denial ("Organizer is not assigned to this
                    // event") on 403, approved-only gating on 403 as well.
                    statusView.text = result.message
                    statusView.setTextColor(ERROR)
                    saveButton.isEnabled = true
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    // Echoes every untouched value back exactly as loaded so the PATCH never alters
    // registration windows, end date, logo, rewards state, or organizer ownership.
    private fun buildRequest(current: OrganizerEventDto, organizerId: java.util.UUID): EventRequest = EventRequest(
        title = titleInput.text.toString().trim(),
        description = descriptionInput.text.toString().trim().ifBlank { null },
        location = venueInput.text.toString().trim().ifBlank { null },
        eventLogoUrl = current.eventLogoUrl,
        registrationOpenAt = current.registrationOpenAt,
        registrationCloseAt = current.registrationCloseAt,
        eventStartAt = requireNotNull(startDateTimeValue).atZone(zoneId).toInstant(),
        eventEndAt = current.eventEndAt,
        capacity = capacityInput.text.toString().trim().toInt(),
        rewardsEnabled = current.rewardsEnabled ?: false,
        organizerUserId = organizerId,
    )

    companion object {
        private val TEXT_COLOR = android.graphics.Color.parseColor("#111827")
    }
}
