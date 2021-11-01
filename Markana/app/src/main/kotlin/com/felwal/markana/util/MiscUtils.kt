package com.felwal.markana.util

import androidx.appcompat.app.AppCompatDelegate
import com.felwal.markana.data.prefs.Theme
import com.felwal.markana.prefs

fun updateDayNight() = AppCompatDelegate.setDefaultNightMode(
    when (prefs.theme) {
        Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        Theme.BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
    }
)