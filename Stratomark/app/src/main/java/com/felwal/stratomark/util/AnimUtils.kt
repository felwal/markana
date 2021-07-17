package com.felwal.stratomark.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import com.google.android.material.floatingactionbutton.FloatingActionButton

const val ANIM_DURATION = 100

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

fun FloatingActionButton.animateFab(@ColorInt fromColor: Int, @ColorInt toColor: Int, toIcon: Drawable?) {
    @SuppressLint("ObjectAnimatorBinding")
    val colorFade = ObjectAnimator.ofInt(this, "backgroundTint", fromColor, toColor)
    colorFade.duration = ANIM_DURATION.toLong()
    colorFade.setEvaluator(ArgbEvaluator())

    colorFade.addUpdateListener { animation: ValueAnimator ->
        val animatedValue = animation.animatedValue as Int
        backgroundTintList = ColorStateList.valueOf(animatedValue)
    }
    colorFade.start()

    setImageDrawable(toIcon)
}