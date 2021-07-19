package com.felwal.stratomark.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.text.Editable
import android.text.Layout
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception

// Context

fun Context.copyToClipboard(text: CharSequence) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.toast(text: String, long: Boolean = false) =
    Toast
        .makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()

fun Activity.safeToast(text: String, long: Boolean = false) = runOnUiThread {
    Toast
        .makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()
}

fun Context.toastLog(tag: String, msg: String, e: Exception? = null) {
    toast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

fun Activity.safeToastLog(tag: String, msg: String, e: Exception? = null) = runOnUiThread {
    toast(msg, true)
    Log.d(tag, msg, e)
    e?.printStackTrace()
}

inline fun <reified A : AppCompatActivity> AppCompatActivity.launchActivity() {
    val intent = Intent(this, A::class.java)
    startActivity(intent)
}

fun Activity.close(): Boolean {
    finish()
    return true
}

fun Activity.hideKeyboard() {
    currentFocus?.hideKeyboard()
}

// View

fun visibleOrGone(visible: Boolean) = if (visible) View.VISIBLE else View.GONE

fun visibleOrNot(visible: Boolean) = if (visible) View.VISIBLE else View.INVISIBLE

fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
    clearFocus()
    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.snackbar(text: String, long: Boolean = true, actionText: String = "", action: ((it: View) -> Unit)? = null) =
    Snackbar
        .make(this, text, if (long) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT)
        .setAction(actionText, action)
        .show()

val EditText.string: String get() = text.toString()

val Editable.copy: Editable
    get() = Editable.Factory.getInstance().newEditable(this)

fun EditText.selectStart() = setSelection(0)

fun EditText.selectEnd() = setSelection(string.length)

fun Layout.getStartOfLine(index: Int): Int = getLineStart(getLineForOffset(index))

// Collection

fun <E> MutableCollection<E>.toggleInclusion(element: E) =
    if (contains(element)) remove(element) else add(element)

fun <E> MutableList<E>.replace(oldElement: E, newElement: E) =
    set(indexOf(oldElement), newElement)

fun <E> MutableCollection<E>.empty() = removeAll(this)

// res

@ColorInt
fun Context.getAttrColor(resId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(resId, typedValue, true)
    return typedValue.data
}

// any

fun <T, O> T.unless(equals: T, block: (it: T) -> O): O? = if (this != equals) block(this) else null

/**
 * Always returns [that]. The opposite of [over].
 */
infix fun <T> Any?.then(that: T): T = that

/**
 * Always returns [this]. The opposite of [then].
 */
infix fun <T> T.over(that: Any?): T = this

/**
 * Returns [this] if [that] isn't of that type.
 *
 * Useful in `when`s when you don't want to return at every branch:
 * `true defaults when (...)`
 */
inline infix fun <reified T> T.defaults(that: Any?): T = if (that is T) that else this

/**
 * Returns [that] if [this] isn't of that type.
 *
 * Useful in `when`s when you don't want to return at every branch:
 * `when (...) {...} default true`
 */
inline infix fun <reified T> Any?.default(that: T): T = if (this is T) this else that