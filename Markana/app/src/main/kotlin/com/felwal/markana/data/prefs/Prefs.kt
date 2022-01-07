package com.felwal.markana.data.prefs

import android.content.Context
import android.content.SharedPreferences

private const val FILENAME = "com.felwal.markana.data.prefs"

// notelist
private const val KEY_SORT_BY = "sort_by"
private const val KEY_SORT_ORDER_REVERSED = "sort_order_reversed"
private const val KEY_GRID_VIEW = "grid_view"

// settings: appearane
private const val KEY_APPEARANCE_THEME = "theme"

// settings: note preview
private const val KEY_PREVIEW_COLOR_ITEMS = "preview_color"
private const val KEY_PREVIEW_LIST_ICON = "preview_list_icon"
private const val KEY_PREVIEW_SHOW_METADATA = "preview_metadata"
private const val KEY_PREVIEW_SHOW_MIME = "preview_mime"
private const val KEY_PREVIEW_MAX_LINES = "preview_max_lines"

// settings: markdown
private const val KEY_MD_SYMBOL_EMPH = "symbol_italic"
private const val KEY_MD_SYMBOL_STRONG = "symbol_bold"
private const val KEY_MD_SYMBOL_BULLET_LIST = "symbol_bullet_list"
private const val KEY_MD_SYMBOL_BREAK = "symbol_horizontal_rule"

// names and values

enum class SortBy {
    NAME,
    MODIFIED,
    OPENED
}

enum class Theme(val title: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark"),
    BATTERY("Set by Battery Saver")
}

enum class Emph(val title: String, val symbol: String) {
    ASTERISK("Asterisk", "*"),
    UNDERSCORE("Underscore", "_")
}

enum class Strong(val title: String, val symbol: String) {
    ASTERISKS("Asterisks", "**"),
    UNDERSCORES("Underscores", "__")
}

enum class Bullet(val title: String, val symbol: String) {
    ASTERISK("Asterisk", "*"),
    DASH("Dash", "-"),
    PLUS("Plus", "+")
}

class Prefs(c: Context) {

    private val sp: SharedPreferences = c.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)

    // notelist

    val sortBy: SortBy get() = SortBy.values()[sortByInt]
    var sortByInt: Int
        get() = sp.getInt(KEY_SORT_BY, 0)
        set(value) = sp.putInt(KEY_SORT_BY, value)

    val ascending: Boolean get() = when (sortBy) {
        SortBy.NAME -> !reverseOrder
        SortBy.MODIFIED -> reverseOrder
        SortBy.OPENED -> reverseOrder
    }
    var reverseOrder: Boolean
        get() = sp.getBoolean(KEY_SORT_ORDER_REVERSED, false)
        set(value) = sp.putBoolean(KEY_SORT_ORDER_REVERSED, value)

    var gridView: Boolean
        get() = sp.getBoolean(KEY_GRID_VIEW, true)
        set(value) = sp.putBoolean(KEY_GRID_VIEW, value)

    // settings: appearance

    val theme: Theme get() = Theme.values()[themeInt]
    var themeInt: Int
        get() = sp.getInt(KEY_APPEARANCE_THEME, 0)
        set(value) = sp.putInt(KEY_APPEARANCE_THEME, value)

    // settings: note preview

    var notePreviewColor: Boolean
        get() = sp.getBoolean(KEY_PREVIEW_COLOR_ITEMS, true)
        set(value) = sp.putBoolean(KEY_PREVIEW_COLOR_ITEMS, value)

    var notePreviewListIcon: Boolean
        get() = sp.getBoolean(KEY_PREVIEW_LIST_ICON, true)
        set(value) = sp.putBoolean(KEY_PREVIEW_LIST_ICON, value)

    var notePreviewMetadata: Boolean
        get() = sp.getBoolean(KEY_PREVIEW_SHOW_METADATA, false)
        set(value) = sp.putBoolean(KEY_PREVIEW_SHOW_METADATA, value)

    var notePreviewMime: Boolean
        get() = sp.getBoolean(KEY_PREVIEW_SHOW_MIME, true)
        set(value) = sp.putBoolean(KEY_PREVIEW_SHOW_MIME, value)

    var notePreviewMaxLines: Int
        get() = sp.getInt(KEY_PREVIEW_MAX_LINES, 12)
        set(value) = sp.putInt(KEY_PREVIEW_MAX_LINES, value)

    // settings: markdown

    val emphSymbol: String get() = Emph.values()[emphSymbolInt].symbol
    var emphSymbolInt: Int
        get() = sp.getInt(KEY_MD_SYMBOL_EMPH, 0)
        set(value) = sp.putInt(KEY_MD_SYMBOL_EMPH, value)

    val strongSymbol: String get() = Strong.values()[strongSymbolInt].symbol
    var strongSymbolInt: Int
        get() = sp.getInt(KEY_MD_SYMBOL_STRONG, 0)
        set(value) = sp.putInt(KEY_MD_SYMBOL_STRONG, value)

    val bulletListSymbol: String get() = Bullet.values()[bulletListSymbolInt].symbol
    var bulletListSymbolInt: Int
        get() = sp.getInt(KEY_MD_SYMBOL_BULLET_LIST, 1)
        set(value) = sp.putInt(KEY_MD_SYMBOL_BULLET_LIST, value)

    var breakSymbol: String
        get() = sp.getString(KEY_MD_SYMBOL_BREAK, "***") ?: "***"
        set(value) = sp.putString(KEY_MD_SYMBOL_BREAK, value)
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) = edit().putBoolean(key, value).apply()

fun SharedPreferences.putFloat(key: String, value: Float) = edit().putFloat(key, value).apply()

fun SharedPreferences.putInt(key: String, value: Int) = edit().putInt(key, value).apply()

fun SharedPreferences.putLong(key: String, value: Long) = edit().putLong(key, value).apply()

fun SharedPreferences.putString(key: String, value: String) = edit().putString(key, value).apply()