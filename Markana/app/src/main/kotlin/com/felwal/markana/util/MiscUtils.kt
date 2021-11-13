package com.felwal.markana.util

import android.content.res.Resources
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import com.felwal.markana.data.prefs.Theme
import com.felwal.markana.prefs
import kotlin.math.round

fun updateDayNight() = AppCompatDelegate.setDefaultNightMode(
    when (prefs.theme) {
        Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        Theme.BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
    }
)

val Int.px: Int get() = round(toFloat().px).toInt()

val Int.dp: Int get() = round(toFloat().dp).toInt()

val Float.px: Float get() =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

val Float.dp: Float get() =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, this, Resources.getSystem().displayMetrics)