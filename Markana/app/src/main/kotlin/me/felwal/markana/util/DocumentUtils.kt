package me.felwal.markana.util

import android.net.Uri
import me.felwal.android.util.split

// mime

fun String.isMime(type: String): Boolean {
    val (thisType, thisSubtype) = split("/", lowerLimit = 2)
    return thisType == type
}

fun String.isMime(type: String, subType: String): Boolean {
    val (thisType, thisSubtype) = split("/", lowerLimit = 2)
    return thisType == type && (thisSubtype == subType || subType == "*")
}

// uri

/**
 * Gets the path part of [Uri.toString], e.i.
 *
 * `"content://com.android.externalstorage.documents/document/home%3ARepositories%2Fnotes%2Fgeneral%2Ffood.txt"`
 *
 * Returns `"document/home%3ARepositories%2Fnotes%2Fgeneral%2Ffood.txt"`
 *
 * @see String.toUriPathString
 */
fun Uri.toUriPathString(): String = toString().toUriPathString()

/**
 * Gets the path part of [Uri.toString], e.i.
 *
 * `"content://com.android.externalstorage.documents/document/home%3ARepositories%2Fnotes%2Fgeneral%2Ffood.txt"`
 *
 * Returns `"document/home%3ARepositories%2Fnotes%2Fgeneral%2Ffood.txt"`
 *
 * @see Uri.toUriPathString
 */
fun String.toUriPathString(): String = substring(lastIndexOf("/") + 1)