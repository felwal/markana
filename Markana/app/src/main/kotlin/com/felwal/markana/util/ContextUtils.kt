package com.felwal.markana.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.felwal.markana.data.prefs.Theme
import com.felwal.markana.prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun updateTheme() = AppCompatDelegate.setDefaultNightMode(
    when (prefs.theme) {
        Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        Theme.BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
    }
)

// lifecycle

inline fun <reified A : AppCompatActivity> AppCompatActivity.launchActivity() {
    val intent = Intent(this, A::class.java)
    startActivity(intent)
}

// toast

fun Context.toast(text: String, long: Boolean = false) =
    Toast.makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()

fun Context.toastLog(tag: String, msg: String, e: Exception? = null) {
    toast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

fun Context.tryToast(text: String, long: Boolean = false) {
    try {
        toast(text, long)
    }
    catch (e: RuntimeException) {
        Log.e("ContextUtils", "toast message: $text", e)
    }
}

fun Context.tryToastLog(tag: String, msg: String, e: Exception? = null) {
    tryToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

suspend fun Context.coToast(text: String, long: Boolean = false) = withUI {
    toast(text, long)
}

suspend fun Context.coToastLog(tag: String, msg: String, e: Exception? = null) {
    coToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

fun Activity.uiToast(text: String, long: Boolean = false) = runOnUiThread {
    toast(text, long)
}

fun Activity.uiToastLog(tag: String, msg: String, e: Exception? = null) = runOnUiThread {
    uiToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

// popup menu

fun <C> C.popup(
    @IdRes anchorRes: Int,
    @MenuRes menuRes: Int
) where C : AppCompatActivity, C : PopupMenu.OnMenuItemClickListener =
    popup(findViewById(anchorRes), menuRes)

fun <C> C.popup(
    anchor: View,
    @MenuRes menuRes: Int
) where C : Context, C : PopupMenu.OnMenuItemClickListener =
    PopupMenu(this, anchor).apply {
        menuInflater.inflate(menuRes, menu)
        setOnMenuItemClickListener(this@popup)
        show()
    }

// res

@ColorInt
fun Context.getAttrColor(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}

// coroutines

suspend fun <T> withUI(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Main, block)

suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, block)