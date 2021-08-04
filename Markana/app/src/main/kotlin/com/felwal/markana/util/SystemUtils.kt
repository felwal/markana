package com.felwal.markana.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.felwal.markana.displayDensity

// screen

val Int.dp: Int get() = (this * displayDensity!! + 0.5f).toInt()

// keyboard

fun Context.copyToClipboard(text: CharSequence) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
    tryToast("Copied to clipboard.")
}

fun Activity.hideKeyboard() {
    currentFocus?.hideKeyboard()
}

fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    clearFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

// orientation

val Activity.orientation: Int get() = getResources().getConfiguration().orientation

val Activity.isPortrait: Boolean get() = orientation == Configuration.ORIENTATION_PORTRAIT

val Activity.isLandscape: Boolean get() = orientation == Configuration.ORIENTATION_LANDSCAPE
