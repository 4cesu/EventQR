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
class IdDisplaySettingsActivity : AppCompatActivity() {

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
            addView(CheckBox(this@IdDisplaySettingsActivity).apply {
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

        val cardView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#25215F"))
                cornerRadius = dp(20).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { setMargins(0, dp(10), 0, dp(4)) }
        }

        cardView.addView(text("ID Preview", 11, true, Color.parseColor("#9B8CF5")).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        })

        // Locked fields first: QR code placeholder box, then attendee name.
        cardView.addView(qrPlaceholder())
        cardView.addView(previewValue("Attendee Name", "Juan Dela Cruz"))

        OPTIONAL_FIELDS.forEach { field ->
            if (fieldStates.getValue(field)) {
                cardView.addView(previewValue(displayName(field), sampleValue(field)))
            }
        }

        previewContainer.addView(cardView)
    }

    private fun qrPlaceholder(): TextView = TextView(this).apply {
        text = "QR CODE"
        gravity = Gravity.CENTER
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setTextColor(Color.parseColor("#25215F"))
        textSize = 15f
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(10).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(84),
        ).apply { setMargins(0, 0, 0, dp(12)) }
    }

    private fun previewValue(label: String, value: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(10))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        addView(text(label.uppercase(), 10, true, Color.parseColor("#9B8CF5")))
        addView(text(value, 16, true, Color.WHITE))
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
        // Known limitation: ATTENDEE_ID is a full UUID from user_profiles.id (36 chars on a printed card).
        FIELD_ATTENDEE_ID -> "b7e23ec2-9f4a-4d01-8c3d-1a2b3c4d5e6f"
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
    }
}
