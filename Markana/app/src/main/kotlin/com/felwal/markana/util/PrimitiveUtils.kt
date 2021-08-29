package com.felwal.markana.util

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// string

fun CharSequence.split(vararg delimiters: String, lowerLimit: Int, ignoreCase: Boolean = false): List<String?> {
    return split(*delimiters, ignoreCase = ignoreCase)
        .toMutableList()
        .toNullable()
        .apply { fillUp(null, lowerLimit) }
}

fun String.isMime(type: String): Boolean {
    val (thisType, thisSubtype) = split("/", lowerLimit = 2)
    return thisType == type
}

fun String.isMime(type: String, subType: String): Boolean {
    val (thisType, thisSubtype) = split("/", lowerLimit = 2)
    return thisType == type && (thisSubtype == subType || subType == "*")
}

// int

/**
 * Clamp an Int to the range [[min], [max]]
 */
fun Int.clamp(min: Int, max: Int): Int = max(min, min(this, max))

fun Int.toColorStateList() = ColorStateList.valueOf(this)

@ColorInt
fun Int.multiplyAlphaComponent(@FloatRange(from = 0.0, to = 1.0) factor: Float): Int {
    val alpha = (Color.alpha(this) * factor).roundToInt().clamp(0, 255)
    return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
}