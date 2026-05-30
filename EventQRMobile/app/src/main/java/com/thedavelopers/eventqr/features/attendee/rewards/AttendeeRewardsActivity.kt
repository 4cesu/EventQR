package com.thedavelopers.eventqr.features.attendee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.RegistrationStatus
import com.thedavelopers.eventqr.core.session.SessionManager
import com.thedavelopers.eventqr.features.rewards.RewardAdapter
import com.thedavelopers.eventqr.features.rewards.model.dto.PointBalanceResponse
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardResponse
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale

open class AttendeeRewardsActivity : AppCompatActivity(), RewardsContract.View {
    private data class RegisteredEventOption(
        val eventId: String,
        val title: String,
    )

    private lateinit var presenter: RewardsPresenter
    private lateinit var repository: AttendeeRepository
    private lateinit var adapter: RewardAdapter
    private lateinit var loadingContainer: View
    private lateinit var loadingText: TextView
    private lateinit var errorContainer: View
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button
    private lateinit var emptyEventsText: TextView
    private lateinit var emptyRewardsText: TextView
    private lateinit var eventSpinner: Spinner
    private lateinit var eventTitleText: TextView
    private lateinit var balanceText: TextView
    private lateinit var rewardsSectionTitle: TextView
    private lateinit var claimsAction: TextView
    private lateinit var rewardsRecycler: RecyclerView
    private lateinit var rewardsBalanceCard: View
    private val eventOptions = mutableListOf<RegisteredEventOption>()
    private var selectedEventId: String? = null
    private var selectedEventTitle: String = ""
    private var attendeeUserId: String? = null
    private var suppressSelectionCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_rewards)
        configureAttendeeBottomNav(AttendeeBottomNavItem.REWARDS)

        repository = AttendeeRepository(this)
        presenter = RewardsPresenter(this, repository)
        adapter = RewardAdapter { reward ->
            val currentEventId = selectedEventId.orEmpty()
            if (currentEventId.isBlank()) {
                Toast.makeText(this, "Select a registered event first.", Toast.LENGTH_SHORT).show()
                return@RewardAdapter
            }

            startActivity(
                Intent(this, RewardDetailsActivity::class.java)
                    .putExtra(EXTRA_EVENT_ID, currentEventId)
                    .putExtra(EXTRA_REWARD_ID, reward.rewardId.toString())
                    .putExtra(EXTRA_REWARD_NAME, reward.name)
                    .putExtra(EXTRA_REWARD_POINTS, reward.pointsRequired)
                    .putExtra(EXTRA_REWARD_STOCK, reward.stockQuantity ?: -1)
            )
        }

        loadingContainer = findViewById(R.id.loadingRewardsContainer)
        loadingText = findViewById(R.id.txtRewardsLoading)
        errorContainer = findViewById(R.id.errorRewardsContainer)
        errorText = findViewById(R.id.txtRewardsError)
        retryButton = findViewById(R.id.btnRewardsRetry)
        emptyEventsText = findViewById(R.id.txtNoRegisteredEvents)
        emptyRewardsText = findViewById(R.id.txtRewardsEmpty)
        eventSpinner = findViewById(R.id.spinnerRegisteredEvents)
        eventTitleText = findViewById(R.id.txtRewardsEventTitle)
        balanceText = findViewById(R.id.txtRewardsBalance)
        rewardsSectionTitle = findViewById(R.id.txtRewardsSectionTitle)
        claimsAction = findViewById(R.id.txtMyClaims)
        rewardsRecycler = findViewById(R.id.recyclerRewards)
        rewardsBalanceCard = findViewById(R.id.cardRewardsBalance)

        rewardsRecycler.apply {
            layoutManager = LinearLayoutManager(this@AttendeeRewardsActivity)
            adapter = this@AttendeeRewardsActivity.adapter
        }

        claimsAction.setOnClickListener {
            val intent = Intent(this, ClaimedRewardsActivity::class.java)
            selectedEventId?.takeIf { it.isNotBlank() }?.let { intent.putExtra(EXTRA_EVENT_ID, it) }
            startActivity(intent)
        }

        retryButton.setOnClickListener {
            if (eventOptions.isEmpty()) {
                loadRegisteredEvents()
            } else {
                val option = selectedEventId?.let { id -> eventOptions.firstOrNull { it.eventId == id } }
                    ?: eventOptions.firstOrNull()
                option?.let { loadSelectedEventRewards(it) }
            }
        }

        eventSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (suppressSelectionCallback) return
                eventOptions.getOrNull(position)?.let { loadSelectedEventRewards(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        attendeeUserId = SessionManager(this).getUserId()
        loadRegisteredEvents()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun showLoading(isLoading: Boolean) {
        loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        loadingText.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            errorContainer.visibility = View.GONE
            errorText.visibility = View.GONE
            retryButton.visibility = View.GONE
            emptyRewardsText.visibility = View.GONE
            rewardsRecycler.visibility = View.GONE
            rewardsBalanceCard.visibility = View.GONE
        }
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        loadingContainer.visibility = View.GONE
        loadingText.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        errorText.text = message.ifBlank { "Unable to load rewards." }
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        emptyEventsText.visibility = View.GONE
        emptyRewardsText.visibility = View.GONE
        rewardsRecycler.visibility = View.GONE
        rewardsBalanceCard.visibility = View.GONE
    }

    override fun showBalance(balance: PointBalanceResponse) {
        rewardsBalanceCard.visibility = View.VISIBLE
        balanceText.text = balance.pointsBalance.toString()
    }

    override fun renderRewards(items: List<RewardResponse>) {
        adapter.submitItems(items)
        loadingContainer.visibility = View.GONE
        loadingText.visibility = View.GONE
        errorContainer.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        rewardsBalanceCard.visibility = View.VISIBLE
        emptyRewardsText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        rewardsRecycler.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadRegisteredEvents() {
        loadingContainer.visibility = View.VISIBLE
        loadingText.text = "Loading attendee rewards..."
        errorContainer.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyEventsText.visibility = View.GONE
        emptyRewardsText.visibility = View.GONE
        rewardsRecycler.visibility = View.GONE
        rewardsBalanceCard.visibility = View.GONE
        eventSpinner.visibility = View.GONE
        rewardsSectionTitle.visibility = View.GONE
        claimsAction.visibility = View.GONE

        lifecycleScope.launch {
            when (val registrationsResult = repository.getMyRegistrations()) {
                is NetworkResult.Success -> {
                    val options = registrationsResult.data
                        .filter { it.status != RegistrationStatus.CANCELLED && it.status != RegistrationStatus.NO_SHOW }
                        .sortedByDescending { it.registeredAt ?: Instant.EPOCH }
                        .distinctBy { it.eventId }
                        .mapNotNull { registration ->
                            val eventId = registration.eventId.toString().takeIf { it.isNotBlank() }
                            val title = registration.eventTitle?.takeIf { it.isNotBlank() }
                                ?: registration.eventId.toString()
                            if (eventId == null) null else RegisteredEventOption(eventId, title)
                        }

                    eventOptions.clear()
                    eventOptions.addAll(options)
                    loadingContainer.visibility = View.GONE

                    if (eventOptions.isEmpty()) {
                        selectedEventId = null
                        selectedEventTitle = ""
                        emptyEventsText.visibility = View.VISIBLE
                        eventSpinner.visibility = View.GONE
                        rewardsSectionTitle.visibility = View.GONE
                        claimsAction.visibility = View.GONE
                        rewardsBalanceCard.visibility = View.GONE
                        rewardsRecycler.visibility = View.GONE
                        emptyRewardsText.visibility = View.GONE
                        return@launch
                    }

                    emptyEventsText.visibility = View.GONE
                    eventSpinner.visibility = View.VISIBLE
                    rewardsSectionTitle.visibility = View.VISIBLE
                    claimsAction.visibility = View.VISIBLE

                    val spinnerAdapter = ArrayAdapter(
                        this@AttendeeRewardsActivity,
                        android.R.layout.simple_spinner_item,
                        eventOptions.map { it.title },
                    ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    eventSpinner.adapter = spinnerAdapter

                    val requestedEventId = intent.getStringExtra(EXTRA_EVENT_ID).orEmpty()
                    val initialIndex = eventOptions.indexOfFirst { it.eventId == requestedEventId }
                        .takeIf { it >= 0 }
                        ?: 0

                    suppressSelectionCallback = true
                    eventSpinner.setSelection(initialIndex, false)
                    eventSpinner.post { suppressSelectionCallback = false }
                    loadSelectedEventRewards(eventOptions[initialIndex])
                }

                is NetworkResult.Error -> {
                    showError(registrationsResult.message.ifBlank { "Unable to load registered events." })
                }

                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun loadSelectedEventRewards(option: RegisteredEventOption) {
        selectedEventId = option.eventId
        selectedEventTitle = option.title
        eventTitleText.text = option.title.uppercase(Locale.getDefault())
        eventTitleText.visibility = View.VISIBLE
        presenter.load(option.eventId, attendeeUserId)
    }
}
