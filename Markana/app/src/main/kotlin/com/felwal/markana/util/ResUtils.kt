package com.felwal.markana.util

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.appcompat.content.res.AppCompatResources

// get res

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? = AppCompatResources.getDrawable(this, id)

fun Context.getBoolean(@BoolRes id: Int): Boolean = resources.getBoolean(id)

fun Context.getDimension(@DimenRes id: Int): Float = resources.getDimension(id)

fun Context.getInteger(@IntegerRes id: Int): Int = resources.getInteger(id)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?): String =
    if (formatArgs.isEmpty()) resources.getQuantityString(id, quantity)
    else resources.getQuantityString(id, quantity, *formatArgs)

fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

fun Context.getIntegerArray(@ArrayRes id: Int): IntArray = resources.getIntArray(id)

// get attr res

@ColorInt
fun Context.getColorAttr(@AttrRes id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}

// combination

fun Context.getDrawableCompat(@DrawableRes id: Int, @AttrRes colorId: Int): Drawable? =
    getDrawableCompat(id)?.withTint(getColorAttr(colorId))

fun Context.getDrawableCompatFilter(@DrawableRes id: Int, @AttrRes colorId: Int): Drawable? =
    getDrawableCompat(id)?.withFilter(getColorAttr(colorId))

// drawable

fun Drawable.withTint(@ColorInt tint: Int): Drawable = mutate().also { setTint(tint) }

fun Drawable.withFilter(@ColorInt tint: Int): Drawable = mutate().also { setColorFilter(tint, PorterDuff.Mode.SRC_IN) }