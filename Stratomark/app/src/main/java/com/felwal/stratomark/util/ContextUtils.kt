package com.felwal.stratomark.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
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

fun Context.safeToast(text: String, long: Boolean = false) {
    try {
        Toast.makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
            .show()
    }
    catch (e: RuntimeException) {
        Log.e("ContextUtils", "toast message: $text", e)
    }
}

fun Activity.uiToast(text: String, long: Boolean = false) = runOnUiThread {
    Toast.makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()
}

fun Context.safeToastLog(tag: String, msg: String, e: Exception? = null) {
    safeToast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
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