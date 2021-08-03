package com.felwal.markana.data.prefs

import android.content.Context
import android.content.SharedPreferences

private const val FILENAME = "com.felwal.markana.data.prefs"

private const val KEY_SORT_BY = "sort_by"
private const val KEY_SORT_ORDER = "sort_order"
private const val KEY_GRID_VIEW = "grid_view"

private const val KEY_THEME = "theme"
private const val KEY_MD_SYMBOL_ITALIC = "italic"
private const val KEY_MD_SYMBOL_BOLD = "bold"
private const val KEY_MD_SYMBOL_BULLETLIST = "bulletlist"
private const val KEY_MD_SYMBOL_HR = "horizontal_rule"
private const val KEY_MD_CHECKBOX_SPACE = "checkbox_space"

// names and values

enum class SortBy {
    NAME,
    MODIFIED,
    OPENED
}

val themeNames = listOf("System default", "Dark", "Light")
val emphSymbols = listOf("*", "_")
val emphSymbolNames = listOf("Asterisk", "Underscore")
val bulletlistSymbols = listOf("*", "-", "+")
val bulletlistSymbolNames = listOf("Asterisk", "Dash", "Plus")

class Prefs(c: Context) {

    private val sp: SharedPreferences = c.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);

    //

    val sortBy: SortBy get() = SortBy.values()[sortByInt]
    var sortByInt: Int
        get() = sp.getInt(KEY_SORT_BY, 0)
        set(value) = sp.putInt(KEY_SORT_BY, value)

    var ascending: Boolean
        get() = sp.getBoolean(KEY_SORT_ORDER, true)
        set(value) = sp.putBoolean(KEY_SORT_ORDER, value)

    var gridView: Boolean
        get() = sp.getBoolean(KEY_GRID_VIEW, true)
        set(value) = sp.putBoolean(KEY_GRID_VIEW, value)

    // settings

    var themeInt: Int
        get() = sp.getInt(KEY_THEME, 0)
        set(value) = sp.putInt(KEY_THEME, value)

    val italicSymbol: String get() = emphSymbols[italicSymbolInt]
    var italicSymbolInt: Int
        get() = sp.getInt(KEY_MD_SYMBOL_ITALIC, 0)
        set(value) = sp.putInt(KEY_MD_SYMBOL_ITALIC, value)

    val boldSymbol: String get() = emphSymbols[boldSymbolInt]
    var boldSymbolInt: Int
        get() = sp.getInt(KEY_MD_SYMBOL_BOLD, 0)
        set(value) = sp.putInt(KEY_MD_SYMBOL_BOLD, value)

    val bulletlistSymbol: String get() = bulletlistSymbols[bulletlistSymbolInt]
    var bulletlistSymbolInt: Int
        get() = sp.getInt(KEY_MD_SYMBOL_BULLETLIST, 0)
        set(value) = sp.putInt(KEY_MD_SYMBOL_BULLETLIST, value)

    var hrSymbol: String
        get() = sp.getString(KEY_MD_SYMBOL_HR, "***") ?: "***"
        set(value) = sp.putString(KEY_MD_SYMBOL_HR, value)

    var checkboxSpace: Boolean
        get() = sp.getBoolean(KEY_MD_CHECKBOX_SPACE, false)
        set(value) = sp.putBoolean(KEY_MD_CHECKBOX_SPACE, value)
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) = edit().putBoolean(key, value).apply()

fun SharedPreferences.putFloat(key: String, value: Float) = edit().putFloat(key, value).apply()

fun SharedPreferences.putInt(key: String, value: Int) = edit().putInt(key, value).apply()

fun SharedPreferences.putLong(key: String, value: Long) = edit().putLong(key, value).apply()

fun SharedPreferences.putString(key: String, value: String) = edit().putString(key, value).apply()