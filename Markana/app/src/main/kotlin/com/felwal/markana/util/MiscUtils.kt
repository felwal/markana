package com.felwal.markana.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felwal.android.util.fillUp
import com.felwal.android.util.toNullable
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

fun <T, VH : RecyclerView.ViewHolder> ListAdapter<T, VH>.submitListKeepScroll(
    list: List<T>,
    manager: RecyclerView.LayoutManager?,
    commitCallback: (() -> Unit)? = null
) {
    //  save state
    val recyclerViewState = manager?.onSaveInstanceState()

    // submit items
    submitList(list) {
        // restore state
        recyclerViewState?.let {
            manager.onRestoreInstanceState(it)
        }

        commitCallback?.invoke()
    }
}

fun CharSequence.split(
    vararg delimiters: String,
    lowerLimit: Int,
    upperLimit: Int,
    ignoreCase: Boolean = false
): List<String?> = split(*delimiters, ignoreCase = ignoreCase, limit = upperLimit)
    .toMutableList()
    .toNullable()
    .apply { fillUp(null, lowerLimit) }