package com.thedavelopers.eventqr.features.organizer.events

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.thedavelopers.eventqr.features.organizer.ghostButton
import com.thedavelopers.eventqr.features.organizer.intentEventId
import com.thedavelopers.eventqr.features.organizer.intentEventTitle
import com.thedavelopers.eventqr.features.organizer.organizerShell
import com.thedavelopers.eventqr.features.organizer.primaryButton
import com.thedavelopers.eventqr.features.organizer.rounded
import com.thedavelopers.eventqr.features.organizer.showMissingEventScreen
import com.thedavelopers.eventqr.features.organizer.text
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * SDD 3.5 (UC-20) — Manage Approved Event Details.
 *
 * Deviation notes (capstone defense):
 * - Built with the programmatic View toolkit used by every other organizer screen (team
 *   decision), not Compose as the module text implies; no ViewModel — repositories plus
 *   coroutine launches inside activities, matching the rest of this codebase.
 * - The full event schedule (registration windows + event start/end) is locked and shown in
 *   a read-only "Schedule (Locked)" section; only title/description/venue/capacity are
 *   editable. The backend rejects a changed start date (409) as a backstop.
 * - SCOPE ADDITION (beyond SRS/SDD UC-16 / SDD 3.1 - event creation poster): the event banner
 *   (poster) is editable post-approval here. It is cropped to 16:9 and uploaded via the shared
 *   /uploads/event-logo endpoint, storing a fileId on events.eventLogoUrl. The backend persists
 *   eventLogoUrl on update (OrganizerService.updateEvent); flag for the capstone deviation log.
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

    private lateinit var regOpenView: TextView
    private lateinit var regCloseView: TextView
    private lateinit var eventStartView: TextView
    private lateinit var eventEndView: TextView

    private lateinit var bannerPreview: ImageView
    private lateinit var bannerStatus: TextView

    private var loadedEvent: OrganizerEventDto? = null
    private var selectedBannerFile: File? = null
    private var newBannerFileId: String? = null

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

    private val bannerPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { launchBannerCrop(it) }
    }

    // SCOPE ADDITION (banner edit post-approval, flagged for capstone deviation log):
    // mirror the 16:9 crop-before-upload flow used at event creation so banners stay landscape.
    private val bannerCropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when {
                result.resultCode == RESULT_OK -> {
                    val croppedUri = result.data?.let { UCrop.getOutput(it) }
                    if (croppedUri != null) {
                        handleCroppedBanner(croppedUri)
                    } else {
                        bannerStatus.text = "Unable to process banner. Please try again."
                        bannerStatus.setTextColor(ERROR)
                    }
                }
                result.resultCode == UCrop.RESULT_ERROR -> {
                    result.data?.let { UCrop.getError(it) }
                    bannerStatus.text = "Unable to crop banner. Please choose another image."
                    bannerStatus.setTextColor(ERROR)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = OrganizerRepository(this)
        eventId = intentEventId() ?: return showMissingEventScreen("Edit Event Details")

        content = organizerShell(
            title = "Edit Event Details",
            subtitle = intentEventTitle()?.takeIf { it.isNotBlank() },
            showBack = true,
        )
        content.addView(buildLockedSchedule())
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

        formCard.addView(text("Event Banner", 15, true, TEXT_COLOR).apply { setPadding(0, dp(18), 0, dp(6)) })
        bannerPreview = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(android.graphics.Color.parseColor("#F3F4F6"), 10, BORDER, density = resources.displayMetrics.density)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(180))
        }
        formCard.addView(bannerPreview)
        formCard.addView(
            text("No banner set. Tap below to choose a 16:9 landscape image.", 12, false, MUTED).apply {
                setPadding(0, dp(8), 0, 0)
            }.also { bannerStatus = it },
        )
        formCard.addView(ghostButton("Choose new banner") { bannerPicker.launch("image/*") }.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)).apply {
                setMargins(0, dp(10), 0, 0)
            }
        })

        formCard.addView(
            text("Locked: eventId, organizer, approval status, registration windows, start date, end date and rewards stay unchanged.", 12, false, MUTED).apply {
                setPadding(0, dp(12), 0, 0)
            },
        )
        return formCard
    }

    private fun buildLockedSchedule(): LinearLayout {
        val scheduleCard = card(16)
        scheduleCard.addView(text("Schedule (Locked)", 15, true, TEXT_COLOR).apply { setPadding(0, 0, 0, dp(4)) })

        regOpenView = addLockedRow(scheduleCard, "Registration Start Date & Time")
        regCloseView = addLockedRow(scheduleCard, "Registration End Date & Time")
        eventStartView = addLockedRow(scheduleCard, "Event Start Date & Time")
        eventEndView = addLockedRow(scheduleCard, "Event End Date & Time")
        return scheduleCard
    }

    private fun addLockedRow(parent: LinearLayout, label: String): TextView {
        parent.addView(text(label, 14, true, TEXT_COLOR).apply { setPadding(0, dp(12), 0, dp(6)) })
        val value = text("-", 14, false, TEXT_COLOR).apply {
            background = rounded(android.graphics.Color.parseColor("#F3F4F6"), 10, BORDER, density = resources.displayMetrics.density)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            isEnabled = false
        }
        parent.addView(value)
        return value
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

    private fun launchBannerCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "event_banner_cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
            setCompressionQuality(85)
            setFreeStyleCropEnabled(false)
        }
        val cropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(16f, 9f)
            .withMaxResultSize(1920, 1080)
            .withOptions(options)
            .getIntent(this)
        bannerCropLauncher.launch(cropIntent)
    }

    private fun handleCroppedBanner(croppedUri: Uri) {
        val croppedFile = File(cacheDir, "event_banner_${System.currentTimeMillis()}.jpg")
        runCatching {
            contentResolver.openInputStream(croppedUri)?.use { input ->
                croppedFile.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Unable to open cropped image")
        }.onSuccess {
            if (croppedFile.length() > MAX_BANNER_BYTES) {
                croppedFile.delete()
                bannerStatus.text = "Banner must not exceed 5 MB. Please choose a smaller image."
                bannerStatus.setTextColor(ERROR)
                return
            }
            selectedBannerFile?.delete()
            selectedBannerFile = croppedFile
            bannerPreview.setImageURI(croppedUri)
            bannerPreview.background = null
            bannerStatus.text = "New banner selected (16:9). It will be uploaded when you save."
            bannerStatus.setTextColor(com.thedavelopers.eventqr.features.organizer.PURPLE)
        }.onFailure {
            bannerStatus.text = "Unable to attach banner. Please choose another image."
            bannerStatus.setTextColor(ERROR)
        }
    }

    private fun loadBannerPreview(current: OrganizerEventDto) {
        val fileId = current.eventLogoUrl?.trim().orEmpty()
        if (fileId.isBlank()) {
            bannerPreview.visibility = View.GONE
            bannerStatus.text = "No banner set. Tap below to choose a 16:9 landscape image."
            return
        }
        MainScope().launch {
            when (val result = repository.getStoredFile(fileId)) {
                is NetworkResult.Success -> {
                    val encoded = result.data?.contentBase64
                    if (encoded.isNullOrBlank()) {
                        bannerPreview.visibility = View.GONE
                        return@launch
                    }
                    runCatching {
                        val bytes = Base64.decode(encoded, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }.onSuccess { bitmap ->
                        if (bitmap != null) {
                            bannerPreview.setImageBitmap(bitmap)
                            bannerPreview.background = null
                            bannerPreview.visibility = View.VISIBLE
                            bannerStatus.text = "Current banner. Tap below to replace it."
                        } else {
                            bannerPreview.visibility = View.GONE
                        }
                    }.onFailure {
                        bannerPreview.visibility = View.GONE
                    }
                }
                is NetworkResult.Error -> bannerPreview.visibility = View.GONE
                NetworkResult.Loading -> Unit
            }
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
        regOpenView.text = event.registrationOpenAt?.let { formatLockedInstant(it) } ?: "-"
        regCloseView.text = event.registrationCloseAt?.let { formatLockedInstant(it) } ?: "-"
        eventStartView.text = event.eventStartAt?.let { formatLockedInstant(it) } ?: "-"
        eventEndView.text = event.eventEndAt?.let { formatLockedInstant(it) } ?: "-"
        loadBannerPreview(event)
        if (isEditLocked(event)) applyEditLock(event)
    }

    private fun formatLockedInstant(instant: java.time.Instant): String =
        java.time.LocalDateTime.ofInstant(instant, zoneId).format(displayFormatter)

    // UC-20 edit lock: details are editable only while the event is Upcoming (Approved).
    // Once it is Active (ongoing) or Completed the whole form is read-only; the backend
    // enforces the same rule (409) as a backstop for stale clients.
    private fun isEditLocked(event: OrganizerEventDto): Boolean =
        event.status.equals("Active", ignoreCase = true) ||
            event.status.equals("Completed", ignoreCase = true)

    private fun applyEditLock(event: OrganizerEventDto) {
        val reason = if (event.status.equals("Active", ignoreCase = true)) "ongoing" else "completed"
        statusView.text = "Editing locked — event is $reason"
        statusView.setTextColor(ERROR)
        listOf(titleInput, descriptionInput, venueInput, capacityInput).forEach { it.isEnabled = false }
        saveButton.isEnabled = false
        saveButton.text = "Editing locked"
    }

    private fun saveChanges() {
        val title = titleInput.text.toString().trim()
        val capacityValue = capacityInput.text.toString().trim().toIntOrNull()

        titleInput.error = if (title.isBlank()) "Title is required" else null
        if (capacityValue == null || capacityValue <= 0) capacityInput.error = "Capacity must be greater than 0"
        if (title.isBlank() || capacityValue == null || capacityValue <= 0) return

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
            // SCOPE ADDITION (banner edit post-approval): if the organizer picked a new banner,
            // upload it first to obtain a fresh fileId, then send the update with that fileId.
            val uploadResult = selectedBannerFile?.let { file ->
                when (val result = repository.uploadEventBanner(file)) {
                    is NetworkResult.Success -> result.data?.fileId?.toString()
                    is NetworkResult.Error -> {
                        statusView.text = result.message.ifBlank { "Could not upload banner. Please try another image." }
                        statusView.setTextColor(ERROR)
                        saveButton.isEnabled = true
                        return@launch
                    }
                    NetworkResult.Loading -> null
                }
            }
            val bannerFileId = uploadResult ?: newBannerFileId ?: current.eventLogoUrl

            when (val result = repository.updateOrganizerEvent(eventId, buildRequest(current, organizerId, bannerFileId))) {
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
    // registration windows, end date, rewards state, or organizer ownership. The banner
    // (eventLogoUrl) reflects any newly chosen/uploaded poster else the loaded value.
    private fun buildRequest(current: OrganizerEventDto, organizerId: java.util.UUID, bannerFileId: String?): EventRequest = EventRequest(
        title = titleInput.text.toString().trim(),
        description = descriptionInput.text.toString().trim().ifBlank { null },
        location = venueInput.text.toString().trim().ifBlank { null },
        eventLogoUrl = bannerFileId,
        registrationOpenAt = current.registrationOpenAt,
        registrationCloseAt = current.registrationCloseAt,
        eventStartAt = requireNotNull(current.eventStartAt),
        eventEndAt = current.eventEndAt,
        capacity = capacityInput.text.toString().trim().toInt(),
        rewardsEnabled = current.rewardsEnabled ?: false,
        organizerUserId = organizerId,
    )

    companion object {
        private val TEXT_COLOR = android.graphics.Color.parseColor("#111827")
        private const val MAX_BANNER_BYTES = 5L * 1024L * 1024L
    }
}
