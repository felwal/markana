package com.felwal.markana.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.Layout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isGone
import androidx.core.view.size
import com.felwal.markana.R
import com.google.android.material.snackbar.Snackbar

const val ANIM_DURATION = 100

// visibility

fun invisibleOrGone(invisible: Boolean): Int = if (invisible) View.INVISIBLE else View.GONE

fun View.hideOrRemove(hide: Boolean) {
    visibility = invisibleOrGone(hide)
}

fun TextView.setTextRemoveIfEmpty(value: String) {
    text = value
    isGone = text == ""
}

// color

/**
 * Shorthand for [View.getBackgroundTintList]
 */
var View.backgroundTint: Int?
    @ColorInt get() = backgroundTintList?.defaultColor
    set(@ColorInt value) {
        backgroundTintList = if (value != null) ColorStateList.valueOf(value) else null
    }

// snackbar

fun View.snackbar(text: String, long: Boolean = true, actionText: String = "", action: ((it: View) -> Unit)? = null) =
    Snackbar.make(this, text, if (long) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT)
        .setAction(actionText, action)
        .show()

// edittext

val EditText.string: String get() = text.toString()

val Editable.copy: Editable
    get() = Editable.Factory.getInstance().newEditable(this)

fun EditText.selectStart() = setSelection(0)

fun EditText.selectEnd() = setSelection(string.length)

fun EditText.makeMultilinePreventEnter() = apply {
    isSingleLine = true
    setHorizontallyScrolling(false) // allow wrapping
    maxLines = 100 // allow expanding
}

//

fun Layout.getStartOfLine(index: Int): Int = getLineStart(getLineForOffset(index))

fun View.enableRipple(c: Context) {
    val attrs = intArrayOf(R.attr.selectableItemBackground)
    val typedArray = c.obtainStyledAttributes(attrs)
    val backgroundResource = typedArray.getResourceId(0, 0)
    setBackgroundResource(backgroundResource)
    typedArray.recycle()
}

// anim

fun View.crossfadeIn(toAlpha: Float) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(toAlpha)
        .setDuration(ANIM_DURATION.toLong())
        .setListener(null)
}

fun View.crossfadeIn() = crossfadeIn(1f)

fun View.crossfadeOut() {
    animate()
        .alpha(0f)
        .setDuration(ANIM_DURATION.toLong())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = View.GONE
            }
        })
}