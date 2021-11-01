package com.felwal.android.util

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import kotlin.math.roundToInt

// string

fun CharSequence.split(vararg delimiters: String, lowerLimit: Int, ignoreCase: Boolean = false): List<String?> {
    return split(*delimiters, ignoreCase = ignoreCase)
        .toMutableList()
        .toNullable()
        .apply { fillUp(null, lowerLimit) }
}

fun CharSequence.coerceSubstring(startIndex: Int, endIndex: Int): String =
    substring(startIndex.coerceIn(0, length), endIndex.coerceIn(0, length))

fun String.findAll(string: String, startIndex: Int = 0, ignoreCase: Boolean = false): List<Int> {
    val key = if (ignoreCase) string.lowercase() else string
    val content = if (ignoreCase) lowercase() else this
    val indices = mutableListOf<Int>()
    val keyLen = key.length

    for (index in startIndex.coerceAtLeast(0)..(content.length - keyLen)) {
        if (content.substring(index, index + keyLen) == key) indices.add(index)
    }

    return indices
}

// int

fun Int.toColorStateList() = ColorStateList.valueOf(this)

@ColorInt
fun Int.multiplyAlphaComponent(@FloatRange(from = 0.0, to = 1.0) factor: Float): Int {
    val alpha = (Color.alpha(this) * factor).roundToInt().coerceIn(0, 255)
    return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
}