package com.felwal.markana.util

import kotlin.math.max
import kotlin.math.min

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
 * Clamp an Int to the range[[min], [max]]
 */
fun Int.clamp(min: Int, max: Int): Int = max(min, min(this, max))