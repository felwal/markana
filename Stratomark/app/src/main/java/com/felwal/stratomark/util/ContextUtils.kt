package com.felwal.stratomark.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.RuntimeException

// lifecycle

inline fun <reified A : AppCompatActivity> AppCompatActivity.launchActivity() {
    val intent = Intent(this, A::class.java)
    startActivity(intent)
}

fun Activity.close(): Boolean {
    finish()
    return true
}

// toast

fun Context.tryToast(text: String, long: Boolean = false) {
    try {
        Toast
            .makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
            .show()
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
    Toast
        .makeText(this@coToast, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()
}

suspend fun Context.coToastLog(tag: String, msg: String, e: Exception? = null) {
    coToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

fun Activity.uiToast(text: String, long: Boolean = false) = runOnUiThread {
    Toast
        .makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()
}

fun Activity.uiToastLog(tag: String, msg: String, e: Exception? = null) = runOnUiThread {
    uiToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

// res

@ColorInt
fun Context.getAttrColor(resId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(resId, typedValue, true)
    return typedValue.data
}

// coroutines

suspend fun <T> withUI(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.Main, block)

suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, block)