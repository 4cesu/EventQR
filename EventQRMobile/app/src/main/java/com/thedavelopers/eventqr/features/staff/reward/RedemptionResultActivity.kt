package com.thedavelopers.eventqr.features.staff.reward

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.features.rewards.AppRewardExtras
import com.thedavelopers.eventqr.features.staff.StaffScreenExtras

class RedemptionResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val approved = intent.getBooleanExtra(StaffScreenExtras.EXTRA_IS_VALID, false)
        val message = intent.getStringExtra(StaffScreenExtras.EXTRA_REASON).orEmpty()
            .ifBlank { if (approved) "Reward redeemed successfully" else "Reward redemption rejected" }
        val points = intent.getIntExtra(StaffScreenExtras.EXTRA_POINTS_DELTA, 0)
        val remaining = intent.getIntExtra(AppRewardExtras.EXTRA_POINTS_BALANCE, 0)
        val rewardName = intent.getStringExtra(AppRewardExtras.EXTRA_REWARD_NAME).orEmpty()

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F7F7FB"))
            setPadding(dp(24), dp(0), dp(24), dp(0))

            addView(LinearLayout(this@RedemptionResultActivity).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(if (approved) R.drawable.bg_card else R.drawable.bg_red_warning)
                setElevation(dp(4).toFloat())
                setPadding(dp(0), dp(0), dp(0), dp(0))

                // Accent header block
                addView(LinearLayout(this@RedemptionResultActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setBackgroundResource(if (approved) R.drawable.bg_reward_points else R.drawable.bg_red_warning)
                    setPadding(dp(24), dp(24), dp(24), dp(24))

                    addView(TextView(this@RedemptionResultActivity).apply {
                        text = if (approved) "✓" else "✕"
                        textSize = 30f
                        gravity = Gravity.CENTER
                        setTextColor(if (approved) Color.parseColor("#065F46") else Color.parseColor("#B91C1C"))
                        setBackgroundResource(
                            if (approved) R.drawable.bg_staff_success_circle else R.drawable.bg_transaction_redeemed_icon
                        )
                        layoutParams = LinearLayout.LayoutParams(dp(64), dp(64))
                    })

                    addView(TextView(this@RedemptionResultActivity).apply {
                        text = if (approved) "REDEMPTION APPROVED" else "REDEMPTION REJECTED"
                        textSize = 18f
                        gravity = Gravity.CENTER
                        setTextColor(if (approved) Color.parseColor("#FFFFFF") else Color.parseColor("#B91C1C"))
                        setTypeface(typeface, Typeface.BOLD)
                        setPadding(0, dp(16), 0, 0)
                    })
                })

                // Details body
                addView(LinearLayout(this@RedemptionResultActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(24), dp(22), dp(24), dp(24))

                    addView(TextView(this@RedemptionResultActivity).apply {
                        text = message
                        textSize = 15f
                        gravity = Gravity.CENTER
                        setTextColor(Color.parseColor("#374151"))
                    })

                    if (rewardName.isNotBlank() && approved) {
                        addView(TextView(this@RedemptionResultActivity).apply {
                            text = "Reward: $rewardName"
                            textSize = 15f
                            gravity = Gravity.CENTER
                            setTextColor(Color.parseColor("#5B25C9"))
                            setTypeface(typeface, Typeface.BOLD)
                            setPadding(0, dp(16), 0, 0)
                        })
                    }

                    addView(TextView(this@RedemptionResultActivity).apply {
                        text = if (approved) {
                            "${points} pts deducted · $remaining pts remaining"
                        } else {
                            "No points were deducted"
                        }
                        textSize = 13f
                        gravity = Gravity.CENTER
                        setTextColor(Color.parseColor("#6B7280"))
                        setPadding(0, dp(10), 0, 0)
                    })
                })
            })

            addView(TextView(this@RedemptionResultActivity).apply {
                text = "Tap to continue scanning"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#5B25C9"))
                setTypeface(typeface, Typeface.BOLD)
                setBackgroundResource(R.drawable.bg_card)
                setPadding(dp(0), dp(14), dp(0), dp(14))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).also { it.topMargin = dp(18) }
                setOnClickListener { finish() }
            })
        })
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
