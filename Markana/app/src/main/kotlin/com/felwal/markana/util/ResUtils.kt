package com.felwal.markana.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.core.content.ContextCompat

fun Context.getDrawableCompat(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

fun Context.getBoolean(@BoolRes id: Int): Boolean = resources.getBoolean(id)

fun Context.getDimension(@DimenRes id: Int): Float = resources.getDimension(id)

fun Context.getInteger(@IntegerRes id: Int): Int = resources.getInteger(id)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int): String =
    resources.getQuantityString(id, quantity)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?): String =
    resources.getQuantityString(id, quantity, *formatArgs)

fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

// attr

@ColorInt
fun Context.getColorByAttr(@AttrRes id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}
