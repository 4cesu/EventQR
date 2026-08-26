package com.thedavelopers.eventqr.features.organizer.idtemplate

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.features.organizer.*
import com.thedavelopers.eventqr.features.registrations.RegistrationNumberFormatter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * SDD Module 3.7 — Configure ID Display Fields.
 *
 * Deviation note (capstone defense): SRS UC-22 describes logo upload, color editing, and
 * predefined template selection; SDD 3.7 explicitly overrides it — "organizer cannot edit the
 * ID layout, design, colors, logo, or visual format." Only field visibility toggling is
 * implemented here. No color picker, logo upload, or template style selector exists in this
 * screen (no leftover scaffolding for those was present to remove).
 *
 * Architecture note: implemented with the programmatic View toolkit used by every other
 * organizer screen (team decision), not Compose as the SDD text implies.
 */
class IdTemplateSettingsActivity : AppCompatActivity() {

    private lateinit var repository: IdTemplateConfigRepository
    private lateinit var eventId: String
    private lateinit var content: LinearLayout
    private lateinit var previewContainer: LinearLayout
    private lateinit var statusView: TextView
    private lateinit var saveButton: Button

    private val fieldStates = linkedMapOf(
        FIELD_ATTENDEE_ID to false,
        FIELD_ROLE to false,
        FIELD_EVENT_NAME to false,
        FIELD_EVENT_DATE to false,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = IdTemplateConfigRepository(this)
        eventId = intentEventId() ?: return showMissingEventScreen("ID Display Settings")

        content = organizerShell(
            title = "ID Display Settings",
            subtitle = intentEventTitle()?.takeIf { it.isNotBlank() },
            showBack = true,
        )
        content.addView(lockedFieldsCard())
        content.addView(toggleCard())
        previewContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(previewContainer)
        renderPreview()
        statusView = text("", 13, false).apply { setPadding(dp(4), dp(8), dp(4), 0) }
        content.addView(statusView)
        saveButton = primaryButton("Save ID Display Settings") { saveConfig() }.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48),
            ).apply { setMargins(0, dp(12), 0, 0) }
        }
        content.addView(saveButton)

        loadConfig()
    }

    private fun lockedFieldsCard(): LinearLayout = card(14).apply {
        addView(sectionLabel("Always included"))
        LOCKED_FIELDS.forEach { addView(lockedRow(displayName(it))) }
    }

    private fun lockedRow(label: String): LinearLayout = row().apply {
        setPadding(0, dp(6), 0, dp(6))
        addView(text(label, 15, false, MUTED).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        addView(chip("Locked", false, MUTED).apply { alpha = 0.75f })
    }

    private fun toggleCard(): LinearLayout = card(14).apply {
        tag = TOGGLES_TAG
        addView(sectionLabel("Show on printed ID"))
        OPTIONAL_FIELDS.forEach { field ->
            addView(CheckBox(this@IdTemplateSettingsActivity).apply {
                text = displayName(field)
                textSize = 15f
                isChecked = fieldStates.getValue(field)
                setPadding(dp(4), dp(6), dp(4), dp(6))
                setOnCheckedChangeListener { _, checked ->
                    fieldStates[field] = checked
                    renderPreview()
                }
            })
        }
    }

    private fun sectionLabel(label: String): TextView = text(label, 13, true).apply {
        setPadding(dp(2), dp(2), dp(2), dp(8))
    }

    private fun renderPreview() {
        previewContainer.removeAllViews()

        // Vertical (portrait) card, CR80 lanyard proportions: 2.125in x 3.375in -> height = width x 1.588.
        val cardView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            // Ink-saving palette: majority white card, black text, thin border — no solid
            // dark fill blocks, since solid fills are the expensive part on printed output.
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(20).toFloat()
                setStroke(dp(1), Color.BLACK)
            }
            layoutParams = LinearLayout.LayoutParams(
                dp(PREVIEW_WIDTH_DP),
                dp(PREVIEW_HEIGHT_DP),
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, dp(10), 0, dp(4))
            }
        }

        cardView.addView(text("ID Preview", 11, true, TEXT).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        })

        // Portrait stacking order: QR code first (locked), then event name banner,
        // attendee name, role, attendee id, event date.
        cardView.addView(qrPlaceholder())
        if (fieldStates.getValue(FIELD_EVENT_NAME)) {
            cardView.addView(previewBanner(displayName(FIELD_EVENT_NAME), sampleValue(FIELD_EVENT_NAME)))
        }
        cardView.addView(previewValue("Attendee Name", "Juan Dela Cruz", valueSizeSp = 20))
        if (fieldStates.getValue(FIELD_ROLE)) {
            cardView.addView(previewValue(displayName(FIELD_ROLE), sampleValue(FIELD_ROLE), valueSizeSp = 13))
        }
        if (fieldStates.getValue(FIELD_ATTENDEE_ID)) {
            cardView.addView(
                previewValue(
                    displayName(FIELD_ATTENDEE_ID),
                    RegistrationNumberFormatter.format(sampleValue(FIELD_ATTENDEE_ID).toIntOrNull()) ?: "N/A",
                    valueSizeSp = 11,
                ),
            )
        }
        if (fieldStates.getValue(FIELD_EVENT_DATE)) {
            cardView.addView(previewValue(displayName(FIELD_EVENT_DATE), sampleValue(FIELD_EVENT_DATE), valueSizeSp = 11))
        }

        previewContainer.addView(cardView)
    }

    private fun previewBanner(label: String, value: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(0, 0, 0, dp(10))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        addView(text(label.uppercase(), 9, true, MUTED, align = Gravity.CENTER_HORIZONTAL))
        addView(text(value, 15, true, TEXT, align = Gravity.CENTER_HORIZONTAL))
    }

    private fun qrPlaceholder(): TextView = TextView(this).apply {
        text = "QR CODE"
        gravity = Gravity.CENTER
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setTextColor(Color.BLACK)
        textSize = 15f
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(10).toFloat()
            setStroke(dp(1), Color.BLACK)
        }
        // Square placeholder, centered horizontally; QR scannability is functional (NFR):
        // this area keeps a fixed generous size regardless of how many fields are shown.
        layoutParams = LinearLayout.LayoutParams(
            dp(120),
            dp(120),
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, dp(12))
        }
    }

    private fun previewValue(label: String, value: String, valueSizeSp: Int = 16): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(0, 0, 0, dp(8))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        addView(text(label.uppercase(), 9, true, MUTED, align = Gravity.CENTER_HORIZONTAL))
        addView(text(value, valueSizeSp, true, TEXT, align = Gravity.CENTER_HORIZONTAL))
    }

    private fun loadConfig() {
        MainScope().launch {
            when (val result = repository.fetchConfig(eventId)) {
                is NetworkResult.Success -> {
                    result.data.visibleFields.filterNotNull().forEach { field ->
                        if (fieldStates.containsKey(field)) fieldStates[field] = true
                    }
                    refreshCheckboxes()
                    renderPreview()
                }

                is NetworkResult.Error -> showStatus(result.message, isError = true)
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun saveConfig() {
        val visibleFields = OPTIONAL_FIELDS.filter { fieldStates.getValue(it) }
        saveButton.isEnabled = false
        MainScope().launch {
            when (val result = repository.saveConfig(eventId, visibleFields)) {
                is NetworkResult.Success -> showStatus("ID display settings saved.", isError = false)
                is NetworkResult.Error -> showStatus(result.message, isError = true)
                NetworkResult.Loading -> Unit
            }
            saveButton.isEnabled = true
        }
    }

    private fun refreshCheckboxes() {
        // Rebuild toggle rows so checkbox state reflects the server response after reload.
        content.findViewWithTag<LinearLayout>(TOGGLES_TAG)?.let { togglesCard ->
            togglesCard.removeAllViews()
            togglesCard.addView(sectionLabel("Show on printed ID"))
            OPTIONAL_FIELDS.forEach { field ->
                togglesCard.addView(CheckBox(this).apply {
                    text = displayName(field)
                    textSize = 15f
                    isChecked = fieldStates.getValue(field)
                    setPadding(dp(4), dp(6), dp(4), dp(6))
                    setOnCheckedChangeListener { _, checked ->
                        fieldStates[field] = checked
                        renderPreview()
                    }
                })
            }
        }
    }

    private fun showStatus(message: String, isError: Boolean) {
        statusView.text = message
        statusView.setTextColor(if (isError) ERROR else SUCCESS)
    }

    private fun displayName(field: String): String = when (field) {
        FIELD_ATTENDEE_ID -> "Attendee ID"
        FIELD_ROLE -> "Role/Type"
        FIELD_EVENT_NAME -> "Event Name"
        FIELD_EVENT_DATE -> "Event Date"
        "QR_CODE" -> "QR Code"
        "ATTENDEE_NAME" -> "Attendee Name"
        else -> field
    }

    // Sample values only; the Module 2.3 print flow renders real data from these same field keys.
    private fun sampleValue(field: String): String = when (field) {
        // ATTENDEE_ID renders event_registrations.registration_number (per-event sequence, V13).
        FIELD_ATTENDEE_ID -> "12"
        FIELD_ROLE -> "STAFF"
        FIELD_EVENT_NAME -> "Freshman Orientation"
        FIELD_EVENT_DATE -> "Aug 25, 2026"
        else -> ""
    }

    companion object {
        const val FIELD_ATTENDEE_ID = "ATTENDEE_ID"
        const val FIELD_ROLE = "ROLE"
        const val FIELD_EVENT_NAME = "EVENT_NAME"
        const val FIELD_EVENT_DATE = "EVENT_DATE"

        val OPTIONAL_FIELDS: List<String> = listOf(FIELD_ATTENDEE_ID, FIELD_ROLE, FIELD_EVENT_NAME, FIELD_EVENT_DATE)
        val LOCKED_FIELDS: List<String> = listOf("QR_CODE", "ATTENDEE_NAME")
        private const val TOGGLES_TAG = "id_display_toggles"

        // CR80 portrait ratio 2.125in x 3.375in (height = width x 1.588), applied to the preview
        // card mock. The print-side renderer must mirror this orientation when it is implemented.
        private const val PREVIEW_WIDTH_DP = 270
        private const val PREVIEW_HEIGHT_DP = 429
    }
}
