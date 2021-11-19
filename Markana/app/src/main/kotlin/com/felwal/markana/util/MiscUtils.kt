package com.felwal.markana.util

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import com.felwal.android.util.getResIdAttr
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

fun Context.getResIdArray(@ArrayRes id: Int): IntArray {
    val typedArray: TypedArray = resources.obtainTypedArray(id)
    val resIds = IntArray(typedArray.length()) { i ->
        typedArray.getResourceId(i, 0)
    }
    typedArray.recycle()
    return resIds
}

val Int.px: Int get() = round(toFloat().px).toInt()

val Int.dp: Int get() = round(toFloat().dp).toInt()

val Float.px: Float get() =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

val Float.dp: Float get() =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, this, Resources.getSystem().displayMetrics)

val SearchView.closeIcon: ImageView get() = findViewById(androidx.appcompat.R.id.search_close_btn)

fun SearchView.fixCloseIconRipple() = closeIcon.setBackgroundResource(
    context.getResIdAttr(android.R.attr.actionBarItemBackground)
)