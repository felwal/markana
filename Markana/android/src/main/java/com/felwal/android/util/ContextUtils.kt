package com.felwal.android.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// lifecycle

inline fun <reified A : AppCompatActivity> AppCompatActivity.launchActivity() {
    val intent = Intent(this, A::class.java)
    startActivity(intent)
}

// toast

fun Context.toast(text: String, long: Boolean = false) =
    Toast.makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()

fun Context.toast(@StringRes textRes: Int, long: Boolean = false) =
    toast(getString(textRes, long))

fun Context.toastLog(tag: String, msg: String, e: Exception? = null) {
    toast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

fun Context.toastLog(tag: String, @StringRes msgRes: Int, e: Exception? = null) =
    toastLog(tag, getString(msgRes), e)

fun Context.tryToast(text: String, long: Boolean = false) {
    try {
        toast(text, long)
    }
    catch (e: RuntimeException) {
        Log.e("ContextUtils", "toast message: $text", e)
    }
}

fun Context.tryToast(@StringRes textRes: Int, long: Boolean = false) =
    tryToast(getString(textRes, long))

fun Context.tryToastLog(tag: String, msg: String, e: Exception? = null) {
    tryToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

fun Context.tryToastLog(tag: String, @StringRes msgRes: Int, e: Exception? = null) =
    tryToastLog(tag, getString(msgRes), e)

suspend fun Context.coToast(text: String, long: Boolean = false) = withUI {
    toast(text, long)
}

suspend fun Context.coToast(@StringRes textRes: Int, long: Boolean = false) =
    coToast(getString(textRes, long))

suspend fun Context.coToastLog(tag: String, msg: String, e: Exception? = null) {
    coToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

suspend fun Context.coToastLog(tag: String, @StringRes msgRes: Int, e: Exception? = null) =
    coToastLog(tag, getString(msgRes), e)

fun Activity.uiToast(text: String, long: Boolean = false) = runOnUiThread {
    toast(text, long)
}

fun Activity.uiToast(@StringRes textRes: Int, long: Boolean = false) =
    uiToast(getString(textRes, long))

fun Activity.uiToastLog(tag: String, msg: String, e: Exception? = null) = runOnUiThread {
    uiToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

fun Activity.uiToastLog(tag: String, @StringRes msgRes: Int, e: Exception? = null) =
    uiToastLog(tag, getString(msgRes), e)

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

// coroutines

suspend fun <T> withUI(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Main, block)

suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, block)