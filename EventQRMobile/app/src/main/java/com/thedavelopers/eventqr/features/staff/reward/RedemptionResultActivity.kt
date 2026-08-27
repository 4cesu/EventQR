package com.thedavelopers.eventqr.features.staff.reward

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        val accent = if (approved) Color.parseColor("#065F46") else Color.parseColor("#B91C1C")

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F7F7FB"))
            setPadding(dp(24), dp(0), dp(24), dp(0))

            addView(TextView(this@RedemptionResultActivity).apply {
                text = if (approved) "✓" else "✕"
                textSize = 54f
                gravity = Gravity.CENTER
                setTextColor(accent)
            })

            addView(TextView(this@RedemptionResultActivity).apply {
                text = if (approved) "REDEMPTION APPROVED" else "REDEMPTION REJECTED"
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(accent)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(12), 0, dp(10))
            })

            addView(TextView(this@RedemptionResultActivity).apply {
                text = message
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#374151"))
            })

            if (rewardName.isNotBlank() && approved) {
                addView(TextView(this@RedemptionResultActivity).apply {
                    text = "Reward: $rewardName"
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setTextColor(Color.parseColor("#5B25C9"))
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(0, dp(14), 0, 0)
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
                setPadding(0, dp(8), 0, 0)
            })

            addView(TextView(this@RedemptionResultActivity).apply {
                text = "Tap to continue scanning"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#5B25C9"))
                setPadding(0, dp(28), 0, 0)
                setOnClickListener { finish() }
            })
        })
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
