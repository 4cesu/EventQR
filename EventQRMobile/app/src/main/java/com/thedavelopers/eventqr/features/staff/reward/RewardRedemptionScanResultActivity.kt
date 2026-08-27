package com.thedavelopers.eventqr.features.staff.reward

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.NetworkResult
import com.thedavelopers.eventqr.core.api.dto.RewardStatus
import com.thedavelopers.eventqr.features.rewards.AppRewardExtras
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionGrantRequest
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardResponse
import com.thedavelopers.eventqr.features.staff.StaffRepository
import com.thedavelopers.eventqr.features.staff.StaffScreenExtras
import com.thedavelopers.eventqr.features.rewards.model.dto.RewardRedemptionResultResponse
import kotlinx.coroutines.launch
import java.util.UUID

class RewardRedemptionScanResultActivity : AppCompatActivity() {

    private val repository by lazy { StaffRepository(this) }
    private var eventId: String = ""
    private var attendeeUserId: String = ""
    private var attendeeName: String = ""
    private var pointsBalance: Int = 0
    private var redemptionScanLogId: String = ""
    private var staffUserId: String = ""
    private var rewardsContainer: LinearLayout? = null
    private var balanceText: TextView? = null
    private var emptyText: TextView? = null
    private var inFlight = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = intent.getStringExtra(StaffScreenExtras.EXTRA_EVENT_ID).orEmpty()
        attendeeUserId = intent.getStringExtra(StaffScreenExtras.EXTRA_ATTENDEE_ID).orEmpty()
        attendeeName = intent.getStringExtra(StaffScreenExtras.EXTRA_ATTENDEE_NAME).orEmpty()
        pointsBalance = intent.getIntExtra(AppRewardExtras.EXTRA_POINTS_BALANCE, 0)
        redemptionScanLogId = intent.getStringExtra(AppRewardExtras.EXTRA_REDEMPTION_SCAN_LOG_ID).orEmpty()
        staffUserId = intent.getStringExtra(StaffScreenExtras.EXTRA_STAFF_USER_ID).orEmpty()
        setContentView(buildUi())
        loadRewards()
    }

    private fun buildUi(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F7F7FB"))

            addView(TextView(this@RewardRedemptionScanResultActivity).apply {
                text = "Reward Redemption"
                textSize = 22f
                setTextColor(Color.parseColor("#151A2D"))
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(20), dp(28), dp(20), dp(16))
            })

            addView(LinearLayout(this@RewardRedemptionScanResultActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(24), dp(20), dp(24), dp(20))
                setBackgroundResource(R.drawable.bg_reward_points)
                setElevation(dp(5).toFloat())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).also { it.setMargins(dp(16), 0, dp(16), 0) }

                addView(TextView(this@RewardRedemptionScanResultActivity).apply {
                    text = attendeeName.ifBlank { "Attendee" }
                    textSize = 16f
                    setTextColor(Color.parseColor("#FFFFFF"))
                    setTypeface(typeface, Typeface.BOLD)
                })

                addView(TextView(this@RewardRedemptionScanResultActivity).apply {
                    text = "Points Balance"
                    textSize = 14f
                    setTextColor(Color.parseColor("#EDE9FE"))
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(0, dp(10), 0, 0)
                })

                balanceText = TextView(this@RewardRedemptionScanResultActivity).apply {
                    text = pointsBalance.toString()
                    textSize = 40f
                    setTextColor(Color.parseColor("#FFFFFF"))
                    setTypeface(typeface, Typeface.BOLD)
                }.also { addView(it) }

                addView(TextView(this@RewardRedemptionScanResultActivity).apply {
                    text = "points available for this event"
                    textSize = 13f
                    setTextColor(Color.parseColor("#EDE9FE"))
                })
            })

            addView(TextView(this@RewardRedemptionScanResultActivity).apply {
                text = "Select a reward to redeem"
                textSize = 14f
                setTextColor(Color.parseColor("#6B7280"))
                setPadding(dp(20), dp(18), dp(20), dp(6))
            })

            rewardsContainer = LinearLayout(this@RewardRedemptionScanResultActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(4), dp(16), dp(16))
            }
            emptyText = TextView(this@RewardRedemptionScanResultActivity).apply {
                text = "No eligible rewards for this attendee."
                textSize = 14f
                setTextColor(Color.parseColor("#6B7280"))
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(20), dp(20), dp(20))
                visibility = View.GONE
            }

            addView(ScrollView(this@RewardRedemptionScanResultActivity).apply {
                addView(LinearLayout(this@RewardRedemptionScanResultActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    rewardsContainer?.let { addView(it) }
                    emptyText?.let { addView(it) }
                })
            })
        }
    }

    private fun loadRewards() {
        if (eventId.isBlank()) {
            showMessage("Missing event context")
            return
        }
        lifecycleScope.launch {
            when (val result = repository.getRewardsByEvent(eventId)) {
                is NetworkResult.Success -> renderRewards(result.data)
                is NetworkResult.Error -> showMessage(result.message.ifBlank { "Unable to load rewards." })
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun renderRewards(rewards: List<RewardResponse>) {
        val list = rewards.filter {
            it.status == RewardStatus.ACTIVE
                    && it.pointsRequired <= pointsBalance
                    && (it.stockQuantity == null || it.stockQuantity > 0)
        }
        val container = rewardsContainer ?: return
        container.removeAllViews()
        if (list.isEmpty()) {
            emptyText?.visibility = View.VISIBLE
            return
        }
        emptyText?.visibility = View.GONE
        list.forEachIndexed { index, reward ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                setBackgroundResource(R.drawable.bg_card)
                setElevation(dp(3).toFloat())
                setOnClickListener { confirmRedemption(reward) }
            }

            val titleRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            titleRow.addView(TextView(this).apply {
                text = reward.name
                textSize = 17f
                setTextColor(Color.parseColor("#151A2D"))
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            titleRow.addView(TextView(this).apply {
                text = "☆ ${reward.pointsRequired} pts"
                textSize = 13f
                setTextColor(Color.parseColor("#4F46E5"))
                setTypeface(typeface, Typeface.BOLD)
                setBackgroundResource(R.drawable.bg_purple_pill)
                setPadding(dp(12), dp(5), dp(12), dp(5))
            })
            card.addView(titleRow)

            val detailRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(12), 0, 0)
            }
            val stock = reward.stockQuantity?.let { "$it left" } ?: "Unlimited stock"
            detailRow.addView(TextView(this).apply {
                text = stock
                textSize = 13f
                setTextColor(Color.parseColor("#6B7280"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            val allowDuplicates = reward.allowDuplicateClaims
            detailRow.addView(TextView(this).apply {
                text = if (allowDuplicates) "Duplicate claims allowed" else "One claim only"
                textSize = 12f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(if (allowDuplicates) Color.parseColor("#B45309") else Color.parseColor("#065F46"))
                setBackgroundResource(if (allowDuplicates) R.drawable.bg_yellow_notice else R.drawable.bg_green_pill)
                setPadding(dp(10), dp(4), dp(10), dp(4))
            })
            card.addView(detailRow)

            container.addView(card)
            if (index < list.size - 1) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(10))
                })
            }
        }
    }

    private fun confirmRedemption(reward: RewardResponse) {
        if (inFlight) return
        android.app.AlertDialog.Builder(this)
            .setTitle("Redeem ${reward.name}")
            .setMessage("This will deduct ${reward.pointsRequired} points from this attendee. Continue?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Redeem") { _, _ -> redeem(reward) }
            .show()
    }

    private fun redeem(reward: RewardResponse) {
        if (inFlight) return
        if (attendeeUserId.isBlank() || redemptionScanLogId.isBlank()) {
            showMessage("Missing redemption context")
            return
        }
        inFlight = true
        val request = RewardRedemptionGrantRequest(
            eventId = UUID.fromString(eventId),
            attendeeUserId = UUID.fromString(attendeeUserId),
            rewardId = reward.rewardId,
            staffUserId = staffUserId.takeIf { it.isNotBlank() }?.let(UUID::fromString),
            redemptionScanLogId = UUID.fromString(redemptionScanLogId),
        )
        lifecycleScope.launch {
            when (val result = repository.redeemRewardStaff(request)) {
                is NetworkResult.Success -> openResult(result.data)
                is NetworkResult.Error -> {
                    inFlight = false
                    showMessage(result.message.ifBlank { "Failed to redeem reward." })
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun openResult(result: RewardRedemptionResultResponse) {
        startActivity(Intent(this, RedemptionResultActivity::class.java).apply {
            putExtra(StaffScreenExtras.EXTRA_IS_VALID, result.status?.name == "REDEEMED")
            putExtra(StaffScreenExtras.EXTRA_REASON, result.reason.orEmpty())
            putExtra(StaffScreenExtras.EXTRA_POINTS_DELTA, result.pointsSpent)
            putExtra(AppRewardExtras.EXTRA_POINTS_BALANCE, result.remainingBalance)
            putExtra(AppRewardExtras.EXTRA_REWARD_NAME, result.rewardName.orEmpty())
        })
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
