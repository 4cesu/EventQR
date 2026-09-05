package com.thedavelopers.eventqr.features.common

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.thedavelopers.eventqr.R

internal fun AppCompatActivity.bindBottomNavItem(
    navId: Int,
    isActive: Boolean,
    iconRes: Int,
    labelText: String,
    destination: Class<out AppCompatActivity>,
    intentExtras: Intent.() -> Unit = {},
) {
    val density = resources.displayMetrics.density
    fun dp(value: Int): Int = (value * density).toInt()

    val container = findViewById<LinearLayout?>(navId) ?: return
    val icon = container.getChildAt(0) as? ImageView ?: return
    val label = container.getChildAt(1) as? TextView ?: return

    icon.layoutParams = LinearLayout.LayoutParams(dp(38), dp(38))
    icon.setPadding(dp(8), dp(8), dp(8), dp(8))
    icon.setImageResource(iconRes)
    icon.setBackgroundResource(if (isActive) R.drawable.bg_nav_icon_active else R.drawable.bg_nav_icon_inactive)
    icon.imageTintList = ColorStateList.valueOf(if (isActive) Color.WHITE else Color.parseColor("#9CA3AF"))

    label.text = labelText
    label.textSize = 12f
    label.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
    label.setTextColor(Color.parseColor(if (isActive) "#312E81" else "#6B7280"))
    val labelLayoutParams = (label.layoutParams as? LinearLayout.LayoutParams)
        ?: LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    labelLayoutParams.topMargin = dp(6)
    label.layoutParams = labelLayoutParams

    container.isClickable = true
    container.isFocusable = true
    container.setOnClickListener {
        if (isActive) return@setOnClickListener

        startActivity(Intent(this@bindBottomNavItem, destination).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intentExtras()
        })
    }
}