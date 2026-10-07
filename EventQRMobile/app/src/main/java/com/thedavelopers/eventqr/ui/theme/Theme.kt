package com.thedavelopers.eventqr.ui.theme

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.thedavelopers.eventqr.R

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = BrandPrimaryLight,
    onPrimaryContainer = TextOnPrimary,
    secondary = BrandPurple,
    onSecondary = TextOnPrimary,
    tertiary = BrandTertiary,
    onTertiary = TextOnPrimary,
    tertiaryContainer = StatusPendingAmberBg,
    onTertiaryContainer = StatusPendingAmberText,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = PaperWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = OutlineVariant,
    error = StatusRejectedRed,
    onError = TextOnPrimary,
    errorContainer = StatusRejectedRedBg,
    onErrorContainer = StatusRejectedRedText,
)

@Composable
fun EventQrTheme(
    spacing: EventQrSpacing = EventQrSpacing(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.applyRequestedEventQrSystemBarAppearance()
        }
    }

    CompositionLocalProvider(LocalSpacing provides spacing) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = EventQrTypography,
            content = content,
        )
    }
}

@Composable
fun EventQrRowTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSpacing provides EventQrSpacing()) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = EventQrTypography,
            content = content,
        )
    }
}

fun Activity.applyEventQrSystemBarAppearance(lightStatusBars: Boolean = false) {
    window.decorView.setTag(R.id.eventqr_light_status_bars, lightStatusBars)
    applyRequestedEventQrSystemBarAppearance()
}

internal fun Activity.applyRequestedEventQrSystemBarAppearance() {
    val lightStatusBars = window.decorView.getTag(R.id.eventqr_light_status_bars) as? Boolean ?: false
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = lightStatusBars
        isAppearanceLightNavigationBars = true
    }
    applyEventQrStatusBarScrim()
}

private fun Activity.applyEventQrStatusBarScrim() {
    val decor = window.decorView as? ViewGroup ?: return
    if (decor.findViewById<View>(R.id.eventqr_status_bar_scrim) != null) return
    val scrim = View(this).apply {
        id = R.id.eventqr_status_bar_scrim
        setBackgroundColor(Color.BLACK)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    decor.addView(scrim, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))
    ViewCompat.setOnApplyWindowInsetsListener(scrim) { view, insets ->
        val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.layoutParams = view.layoutParams.apply { height = statusTop }
        insets
    }
    ViewCompat.requestApplyInsets(scrim)
}

fun View.applyEventQrTopInsetPadding() {
    val basePaddingTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.updatePadding(top = basePaddingTop + statusTop)
        insets
    }
    // Views attached after the first insets pass (e.g. headers built after a network call) would otherwise never get them.
    ViewCompat.requestApplyInsets(this)
}
