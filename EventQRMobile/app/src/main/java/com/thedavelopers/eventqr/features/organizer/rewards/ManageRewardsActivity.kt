package com.thedavelopers.eventqr.features.organizer.rewards

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.thedavelopers.eventqr.core.api.ApiConfig
import com.thedavelopers.eventqr.core.api.AuthInterceptor
import com.thedavelopers.eventqr.core.api.InstantTypeAdapter
import com.thedavelopers.eventqr.core.api.dto.ApiResponse
import com.thedavelopers.eventqr.core.api.dto.RedemptionStatus
import com.thedavelopers.eventqr.core.api.dto.RewardStatus
import com.thedavelopers.eventqr.core.session.SessionManager
import com.thedavelopers.eventqr.features.organizer.BG
import com.thedavelopers.eventqr.features.organizer.BORDER
import com.thedavelopers.eventqr.features.organizer.ERROR
import com.thedavelopers.eventqr.features.organizer.MUTED
import com.thedavelopers.eventqr.features.organizer.NAV_EVENTS
import com.thedavelopers.eventqr.features.organizer.OrganizerMvpEvent
import com.thedavelopers.eventqr.features.organizer.OrganizerRepository
import com.thedavelopers.eventqr.features.organizer.PRIMARY
import com.thedavelopers.eventqr.features.organizer.PURPLE
import com.thedavelopers.eventqr.features.organizer.SUCCESS
import com.thedavelopers.eventqr.features.organizer.TEXT
import com.thedavelopers.eventqr.features.organizer.card
import com.thedavelopers.eventqr.features.organizer.dp
import com.thedavelopers.eventqr.features.organizer.emptyState
import com.thedavelopers.eventqr.features.organizer.errorState
import com.thedavelopers.eventqr.features.organizer.formatCount
import com.thedavelopers.eventqr.features.organizer.intentEventId
import com.thedavelopers.eventqr.features.organizer.organizerRefreshShell
import com.thedavelopers.eventqr.features.organizer.resolveSelectedEvent
import com.thedavelopers.eventqr.features.organizer.rounded
import com.thedavelopers.eventqr.features.organizer.saveSelectedEventId
import com.thedavelopers.eventqr.features.organizer.section
import com.thedavelopers.eventqr.features.organizer.selectedEventId
import com.thedavelopers.eventqr.features.organizer.showMissingEventScreen
import com.thedavelopers.eventqr.features.organizer.text
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionResponse
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRequest
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardResponse
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

open class ManageRewardsActivity : AppCompatActivity() {
    private lateinit var repository: OrganizerRepository
    private lateinit var selectedEvent: OrganizerMvpEvent
    private lateinit var content: LinearLayout
    private lateinit var eventSpinner: Spinner
    private lateinit var eventSummaryTitle: TextView
    private lateinit var eventSummaryCount: TextView
    private lateinit var rewardsEnabledSwitch: SwitchCompat
    private lateinit var rewardHost: LinearLayout
    private lateinit var refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

    private val rewardsService by lazy { OrganizerRewardsApiProvider.get(this) }
    private var rewards: List<RewardResponse> = emptyList()
    private var redemptions: List<RewardRedemptionResponse> = emptyList()
    private var eventOptions: List<OrganizerMvpEvent> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = OrganizerRepository(this)

        eventOptions = repository.getApprovedOrganizerEvents()
        val requestedEventId = intentEventId() ?: selectedEventId().takeIf { it.isNotBlank() }
        selectedEvent = resolveSelectedEvent(eventOptions, requestedEventId)
            ?: return showMissingEventScreen("Organizer / Rewards")

        val shell = organizerRefreshShell(
            title = "Organizer / Rewards",
            selectedNav = NAV_EVENTS,
            showBack = true,
            topRightLabel = "+ Add",
            onTopRight = { showRewardDialog(null) },
            onRefresh = { loadRewards(showInitialLoading = false) },
        )
        content = shell.content
        refreshLayout = shell.swipeRefreshLayout
        buildScreen()
        loadRewards()
    }

    private fun buildScreen() {
        content.removeAllViews()
        content.setBackgroundColor(BG)

        content.addView(text("Event", 13, true, MUTED).apply {
            setPadding(0, 0, 0, dp(8))
        })

        eventSpinner = Spinner(this).apply {
            background = rounded(Color.WHITE, 10, BORDER, density = resources.displayMetrics.density)
            setPadding(dp(12), 0, dp(12), 0)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48),
            )
            adapter = ArrayAdapter(
                this@ManageRewardsActivity,
                android.R.layout.simple_spinner_item,
                eventOptions.map { it.title.ifBlank { "Untitled Event" } },
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            val selectedIndex = eventOptions.indexOfFirst { it.id == selectedEvent.id }.coerceAtLeast(0)
            setSelection(selectedIndex, false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val event = eventOptions.getOrNull(position) ?: return
                    if (event.id == selectedEvent.id) return
                    selectedEvent = event
                    repository.saveSelectedEventId(event.id)
                    saveSelectedEventId(event.id)
                    bindEventSummary()
                    loadRewards()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
        content.addView(eventSpinner)

        content.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(Color.parseColor("#EEF2FF"), 12, null, density = resources.displayMetrics.density)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { setMargins(0, dp(14), 0, dp(14)) }

            eventSummaryTitle = text(selectedEvent.title, 13, true, PURPLE).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            eventSummaryCount = text("0 rewards", 13, false, MUTED)
            addView(eventSummaryTitle)
            addView(eventSummaryCount)
        })

        content.addView(card(16).apply {
            val row = LinearLayout(this@ManageRewardsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            row.addView(LinearLayout(this@ManageRewardsActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                addView(text("Event Rewards", 16, true, TEXT))
                addView(text("Enable or disable reward redemption for this event", 13, false, MUTED).apply {
                    setPadding(0, dp(4), dp(8), 0)
                })
            })
            rewardsEnabledSwitch = SwitchCompat(this@ManageRewardsActivity).apply {
                isChecked = isRewardsEnabled()
                setOnCheckedChangeListener { _, checked -> setRewardsEnabled(checked) }
            }
            row.addView(rewardsEnabledSwitch)
            addView(row)
        })

        content.addView(section("Rewards"))
        rewardHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(rewardHost)
        bindEventSummary()
    }

    private fun bindEventSummary() {
        eventSummaryTitle.text = selectedEvent.title
        eventSummaryCount.text = if (rewards.size == 1) "1 reward" else "${rewards.size} rewards"
        rewardsEnabledSwitch.setOnCheckedChangeListener(null)
        rewardsEnabledSwitch.isChecked = isRewardsEnabled()
        rewardsEnabledSwitch.setOnCheckedChangeListener { _, checked -> setRewardsEnabled(checked) }
    }

    private fun loadRewards(showInitialLoading: Boolean = true) {
        if (showInitialLoading && !refreshLayout.isRefreshing) {
            rewardHost.removeAllViews()
            rewardHost.addView(text("Loading rewards...", 14, false, MUTED).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(24), 0, dp(24))
            })
        }

        val eventId = selectedEvent.id
        lifecycleScope.launch {
            try {
                val rewardsResponse = rewardsService.getRewards(eventId)
                val redemptionsResponse = rewardsService.getClaimedRewards(eventId)
                if (selectedEvent.id != eventId) {
                    refreshLayout.isRefreshing = false
                    return@launch
                }
                if (!rewardsResponse.success) {
                    throw IllegalStateException(rewardsResponse.message ?: "Unable to load rewards.")
                }
                rewards = rewardsResponse.data.orEmpty().sortedBy { it.name.lowercase() }
                redemptions = redemptionsResponse.data.orEmpty()
                refreshLayout.isRefreshing = false
                renderRewards()
            } catch (error: Exception) {
                if (selectedEvent.id != eventId) return@launch
                refreshLayout.isRefreshing = false
                rewardHost.removeAllViews()
                rewardHost.addView(errorState(error.message ?: "Unable to load rewards.") { loadRewards() })
                Toast.makeText(this@ManageRewardsActivity, error.message ?: "Unable to load rewards.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderRewards() {
        bindEventSummary()
        rewardHost.removeAllViews()
        val enabled = isRewardsEnabled()
        if (rewards.isEmpty()) {
            rewardHost.addView(emptyState("No rewards have been created yet.", "Add Reward") { showRewardDialog(null) })
            return
        }

        val claimedByReward = redemptions
            .filter { it.status == RedemptionStatus.REDEEMED }
            .groupingBy { it.rewardId }
            .eachCount()

        rewards.forEach { reward ->
            val claimed = claimedByReward[reward.rewardId] ?: 0
            rewardHost.addView(rewardCard(reward, claimed, enabled))
        }
    }

    private fun rewardCard(reward: RewardResponse, claimed: Int, rewardsEnabled: Boolean): LinearLayout {
        val stock = reward.stockQuantity
        val outOfStock = stock != null && stock <= claimed
        val active = rewardsEnabled && reward.status == RewardStatus.ACTIVE && !outOfStock
        val badgeText = when {
            !rewardsEnabled || reward.status == RewardStatus.INACTIVE -> "Disabled"
            outOfStock -> "Out of Stock"
            else -> "Available"
        }
        val badgeColor = when (badgeText) {
            "Available" -> SUCCESS
            "Out of Stock" -> ERROR
            else -> MUTED
        }

        return card(16).apply {
            alpha = if (active) 1f else 0.82f

            val header = LinearLayout(this@ManageRewardsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            header.addView(text(reward.name, 17, true, TEXT).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            header.addView(text(badgeText, 12, true, badgeColor).apply {
                setPadding(dp(10), dp(5), dp(10), dp(5))
                background = rounded(
                    when (badgeText) {
                        "Available" -> Color.parseColor("#D1FAE5")
                        "Out of Stock" -> Color.parseColor("#FEE2E2")
                        else -> Color.parseColor("#E5E7EB")
                    },
                    16,
                    null,
                    density = resources.displayMetrics.density,
                )
            })
            addView(header)

            addView(text(reward.description?.takeIf { it.isNotBlank() } ?: "Reward item redeemable using event points.", 13, false, MUTED).apply {
                setPadding(0, dp(5), 0, dp(12))
            })

            val meta = LinearLayout(this@ManageRewardsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            meta.addView(metaText("☆ ${formatCount(reward.pointsRequired)} pts"))
            meta.addView(metaText(if (stock == null) "${formatCount(claimed)} claimed" else "${formatCount(claimed)}/${formatCount(stock)} claimed"))
            meta.addView(metaText(if (active) "Redemption open" else badgeText))
            addView(meta)

            val actions = LinearLayout(this@ManageRewardsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply { setMargins(0, dp(14), 0, 0) }
            }
            actions.addView(actionButton("Edit", false) { showRewardDialog(reward) })
            actions.addView(actionButton("Remove", true) { confirmDeleteReward(reward) })
            addView(actions)
        }
    }

    private fun metaText(value: String): TextView = text(value, 12, false, MUTED).apply {
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }

    private fun actionButton(label: String, destructive: Boolean, onClick: () -> Unit): TextView = text(label, 14, true, if (destructive) Color.WHITE else PURPLE).apply {
        gravity = Gravity.CENTER
        setPadding(0, dp(12), 0, dp(12))
        background = rounded(
            if (destructive) Color.parseColor("#EF4444") else Color.parseColor("#EEF2FF"),
            10,
            null,
            density = resources.displayMetrics.density,
        )
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(if (destructive) dp(8) else 0, 0, 0, 0)
        }
        setOnClickListener { onClick() }
    }

    private fun showRewardDialog(reward: RewardResponse?) {
        val isEdit = reward != null
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), 0)
        }
        val nameInput = EditText(this).apply {
            hint = "Reward name"
            setText(reward?.name.orEmpty())
            singleLine()
        }
        val pointsInput = EditText(this).apply {
            hint = "Points required"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(reward?.pointsRequired?.toString().orEmpty())
            singleLine()
        }
        val stockInput = EditText(this).apply {
            hint = "Stock quantity"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(reward?.stockQuantity?.toString().orEmpty())
            singleLine()
        }
        form.addView(fieldLabel("Reward Name"))
        form.addView(nameInput)
        form.addView(fieldLabel("Points Required"))
        form.addView(pointsInput)
        form.addView(fieldLabel("Stock Quantity"))
        form.addView(stockInput)

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Edit Reward" else "Add Reward")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (isEdit) "Save" else "Create") { _, _ ->
                val name = nameInput.text.toString().trim()
                val points = pointsInput.text.toString().toIntOrNull()
                val stock = stockInput.text.toString().toIntOrNull()
                if (name.isBlank() || points == null || points < 0) {
                    Toast.makeText(this, "Enter a valid reward name and points.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                saveReward(reward, name, points, stock)
            }
            .show()
    }

    private fun fieldLabel(label: String): TextView = text(label, 13, true, TEXT).apply {
        setPadding(0, dp(12), 0, dp(4))
    }

    private fun saveReward(existingReward: RewardResponse?, name: String, points: Int, stock: Int?) {
        val eventId = selectedEvent.id
        val request = RewardRequest(UUID.fromString(eventId), name, points, stock)
        lifecycleScope.launch {
            try {
                val response = if (existingReward == null) {
                    rewardsService.createReward(eventId, request)
                } else {
                    rewardsService.updateReward(eventId, existingReward.rewardId.toString(), request)
                }
                if (!response.success) throw IllegalStateException(response.message ?: "Reward could not be saved.")
                Toast.makeText(this@ManageRewardsActivity, response.message ?: "Reward saved.", Toast.LENGTH_SHORT).show()
                loadRewards()
            } catch (error: Exception) {
                Toast.makeText(this@ManageRewardsActivity, error.message ?: "Reward could not be saved.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDeleteReward(reward: RewardResponse) {
        AlertDialog.Builder(this)
            .setTitle("Remove reward?")
            .setMessage("Remove ${reward.name} from ${selectedEvent.title}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Remove") { _, _ -> deleteReward(reward) }
            .show()
    }

    private fun deleteReward(reward: RewardResponse) {
        val eventId = selectedEvent.id
        lifecycleScope.launch {
            try {
                val response = rewardsService.deleteReward(eventId, reward.rewardId.toString())
                if (!response.success) throw IllegalStateException(response.message ?: "Reward could not be removed.")
                Toast.makeText(this@ManageRewardsActivity, response.message ?: "Reward removed.", Toast.LENGTH_SHORT).show()
                loadRewards()
            } catch (error: Exception) {
                Toast.makeText(this@ManageRewardsActivity, error.message ?: "Reward could not be removed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isRewardsEnabled(): Boolean {
        val defaultEnabled = selectedEvent.rewardsStatus.equals("Enabled", ignoreCase = true) || rewards.isNotEmpty()
        return getSharedPreferences("organizer_rewards_state", Context.MODE_PRIVATE)
            .getBoolean("rewards_enabled_${selectedEvent.id}", defaultEnabled)
    }

    private fun setRewardsEnabled(enabled: Boolean) {
        getSharedPreferences("organizer_rewards_state", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("rewards_enabled_${selectedEvent.id}", enabled)
            .apply()
        renderRewards()
        Toast.makeText(
            this,
            if (enabled) "Reward redemption enabled for this event." else "Reward redemption disabled for this event.",
            Toast.LENGTH_SHORT,
        ).show()
    }
}

private interface OrganizerRewardsService {
    @GET("organizer/events/{eventId}/rewards")
    suspend fun getRewards(@Path("eventId") eventId: String): ApiResponse<List<RewardResponse>>

    @POST("organizer/events/{eventId}/rewards")
    suspend fun createReward(
        @Path("eventId") eventId: String,
        @Body request: RewardRequest,
    ): ApiResponse<RewardResponse>

    @PATCH("organizer/events/{eventId}/rewards/{rewardId}")
    suspend fun updateReward(
        @Path("eventId") eventId: String,
        @Path("rewardId") rewardId: String,
        @Body request: RewardRequest,
    ): ApiResponse<RewardResponse>

    @DELETE("organizer/events/{eventId}/rewards/{rewardId}")
    suspend fun deleteReward(
        @Path("eventId") eventId: String,
        @Path("rewardId") rewardId: String,
    ): ApiResponse<Unit>

    @GET("organizer/events/{eventId}/claimed-rewards")
    suspend fun getClaimedRewards(@Path("eventId") eventId: String): ApiResponse<List<RewardRedemptionResponse>>
}

private object OrganizerRewardsApiProvider {
    @Volatile
    private var service: OrganizerRewardsService? = null

    fun get(context: Context): OrganizerRewardsService {
        return service ?: synchronized(this) {
            service ?: build(context.applicationContext).also { service = it }
        }
    }

    private fun build(context: Context): OrganizerRewardsService {
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeAdapter)
            .setLenient()
            .create()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(SessionManager(context)))
            .build()
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OrganizerRewardsService::class.java)
    }
}
