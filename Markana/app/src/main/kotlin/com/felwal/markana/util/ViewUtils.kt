package com.felwal.markana.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.text.Editable
import android.text.Layout
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.felwal.markana.R
import com.google.android.material.snackbar.Snackbar

const val ANIM_DURATION = 100

// visibility

fun visibleOrNot(visible: Boolean): Int = if (visible) View.VISIBLE else View.INVISIBLE

fun visibleOrGone(visible: Boolean): Int = if (visible) View.VISIBLE else View.GONE

fun invisibleOrGone(invisible: Boolean): Int = if (invisible) View.INVISIBLE else View.GONE

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.remove() {
    visibility = View.GONE
}

fun View.showOrHide(show: Boolean) {
    visibility = visibleOrNot(show)
}

fun View.showOrRemove(show: Boolean) {
    visibility = visibleOrGone(show)
}

fun View.hideOrRemove(hide: Boolean) {
    visibility = invisibleOrGone(hide)
}

fun TextView.removeIfEmpty() = showOrRemove(text != "")

fun TextView.setTextRemoveIfEmpty(value: String) {
    text = value
    removeIfEmpty()
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