package com.felwal.android.util

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources

// get res

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? = AppCompatResources.getDrawable(this, id)

fun Context.getBoolean(@BoolRes id: Int): Boolean = resources.getBoolean(id)

fun Context.getDimension(@DimenRes id: Int): Float = resources.getDimension(id)

fun Context.getInteger(@IntegerRes id: Int): Int = resources.getInteger(id)

fun Context.getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?): String =
    if (formatArgs.isEmpty()) resources.getQuantityString(id, quantity, quantity)
    else resources.getQuantityString(id, quantity, *formatArgs)

fun Context.getStringArray(@ArrayRes id: Int): Array<String> = resources.getStringArray(id)

fun Context.getIntegerArray(@ArrayRes id: Int): IntArray = resources.getIntArray(id)

fun Context.getStyle(@StyleRes id: Int): Int = resources.getIdentifier("CustomDigitsTheme", "style", packageName)

// get attr res

/**
 * Gets resource id from attribute [id].
 */
fun Context.getIdAttr(@AttrRes id: Int): Int {
    val attrs = intArrayOf(id)
    val typedArray = obtainStyledAttributes(attrs)
    val backgroundResource = typedArray.getResourceId(0, 0)
    typedArray.recycle()
    return backgroundResource
}

@ColorInt
fun Context.getColorAttr(@AttrRes id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}

fun Context.getDrawableAttr(@AttrRes id: Int): Drawable? = getDrawableCompat(getIdAttr(id))

fun Context.getBooleanAttr(@AttrRes id: Int): Boolean = getBoolean(getIdAttr(id))

fun Context.getDimensionAttr(@AttrRes id: Int): Float = getDimension(getIdAttr(id))

fun Context.getIntegerAttr(@AttrRes id: Int): Int = getInteger(getIdAttr(id))

fun Context.getQuantityStringAttr(@AttrRes id: Int, quantity: Int, vararg formatArgs: Any?): String =
    getQuantityString(getIdAttr(id), quantity, formatArgs)

fun Context.getStringArrayAttr(@AttrRes id: Int): Array<String> = getStringArray(getIdAttr(id))

fun Context.getIntegerArrayAttr(@AttrRes id: Int): IntArray = getIntegerArray(getIdAttr(id))

fun Context.getStyleAttr(@AttrRes id: Int): Int = getStyle(getIdAttr(id))

// combination

fun Context.getDrawableCompat(@DrawableRes id: Int, @AttrRes colorId: Int): Drawable? =
    getDrawableCompat(id)?.withTint(getColorAttr(colorId))

fun Context.getDrawableCompatFilter(@DrawableRes id: Int, @AttrRes colorId: Int): Drawable? =
    getDrawableCompat(id)?.withFilter(getColorAttr(colorId))

// drawable

fun Drawable.withTint(@ColorInt tint: Int): Drawable = mutate().also { setTint(tint) }

fun Drawable.withFilter(@ColorInt tint: Int): Drawable = mutate().also { setColorFilter(tint, PorterDuff.Mode.SRC_IN) }