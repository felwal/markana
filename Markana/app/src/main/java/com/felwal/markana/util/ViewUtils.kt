package com.felwal.markana.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Layout
import android.view.View
import android.widget.EditText
import androidx.annotation.ColorInt
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

const val ANIM_DURATION = 100

// visibility

fun visibleOrGone(visible: Boolean) = if (visible) View.VISIBLE else View.GONE

fun visibleOrNot(visible: Boolean) = if (visible) View.VISIBLE else View.INVISIBLE

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

fun Layout.getStartOfLine(index: Int): Int = getLineStart(getLineForOffset(index))

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