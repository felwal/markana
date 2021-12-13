package com.felwal.markana.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.felwal.android.util.backgroundTint
import com.felwal.android.util.getColorByAttr
import com.felwal.android.util.getDrawableCompatWithFilter
import com.felwal.markana.R
import com.felwal.markana.databinding.ItemFabMenuBinding
import com.felwal.markana.databinding.ItemFabMenuItemBinding
import com.felwal.markana.databinding.ItemFabMenuOverlayBinding
import com.felwal.markana.util.sign
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabMenu(
    context: Context,
    private val inflater: LayoutInflater,
    parent: ViewGroup
) {
    var isMenuOpen = false

    private val binding = ItemFabMenuBinding.inflate(inflater, parent, true)
    private val overlayBinding = ItemFabMenuOverlayBinding.inflate(inflater, parent, true)
    private val itemBindings: MutableList<ItemFabMenuItemBinding> = mutableListOf()

    private val overlayAlpha = 0.96f
    private val animDuration = 150L
    private val animRotation = 90f + 45f

    private val fabClosedColor = context.getColorByAttr(R.attr.colorSecondary)
    private val fabOpenedColor = context.getColorByAttr(R.attr.colorSurface)
    private val fabMiniColor = context.getColorByAttr(R.attr.colorSecondary)

    private val iconClosedColorAttr = R.attr.colorOnSecondary
    private val iconOpenedColorAttr = R.attr.colorControlActivated
    private val iconMiniColorAttr = R.attr.colorOnSecondary

    private val iconClosedDrawable = context.getDrawableCompatWithFilter(R.drawable.ic_add_24, iconClosedColorAttr)

    private val animators: MutableList<ViewPropertyAnimator> = mutableListOf()

    //

    init {
        binding.ivFabClosedIcon.isVisible = false
        binding.ivFabOpenedIcon.isVisible = false

        // open/close
        binding.fab.setOnClickListener {
            if (isMenuOpen) closeMenu() else openMenu()
        }

        // close on outside click
        overlayBinding.root.setOnClickListener { closeMenu() }
    }

    //

    fun updateVisibilityOnScroll(dy: Int) {
        if (binding.fab.isOrWillBeShown && dy > 0) {
            binding.fab.hide()
            //closeMenu()
        }
        else if (binding.fab.isOrWillBeHidden && dy < 0) binding.fab.show()
    }

    fun addItem(title: String, icon: Drawable?, onClick: (View) -> Unit) {
        val itemBinding = ItemFabMenuItemBinding.inflate(inflater, binding.root, false)
        binding.root.addView(itemBinding.root, 0)

        itemBinding.tvMenuItemTitle.text = title
        itemBinding.fabMenuItem.setImageDrawable(icon)
        itemBinding.fabMenuItem.setOnClickListener(onClick)

        itemBindings.add(itemBinding)
    }

    //

    fun openMenu() {
        // container
        binding.fab.animateFab(fabClosedColor, fabOpenedColor)

        // cancel animations
        animators.forEach { it.cancel() }
        animators.clear()

        // icons
        binding.ivFabClosedIcon.apply {
            rotate(0f, animRotation)
            isVisible = true
            // remove fab icon, as we cannot rotate it (since that rotates the container as well)
            binding.fab.setImageDrawable(null)
            animators += crossfadeOut(animDuration)
        }
        binding.ivFabOpenedIcon.apply {
            animators += crossfadeIn(animDuration)
            rotate(-animRotation, 0f)
        }

        // items
        for (itemBinding in itemBindings) {
            itemBinding.fabMenuItem.show()
            animators += itemBinding.root.crossfadeIn(animDuration)
        }

        // overlay
        animators += overlayBinding.root.crossfadeIn(animDuration, overlayAlpha)

        isMenuOpen = true
    }

    fun closeMenu() {
        // container
        binding.fab.animateFab(fabOpenedColor, fabClosedColor)

        // cancel animations
        animators.forEach { it.cancel() }
        animators.clear()

        // icons
        binding.ivFabClosedIcon.apply {
            rotate(animRotation, 0f)
            animators += crossfadeIn(animDuration) {
                // readd fab icon, as we want the fab hiding animation
                binding.fab.setImageDrawable(iconClosedDrawable)
                isVisible = false
            }
        }
        binding.ivFabOpenedIcon.apply {
            animators += crossfadeOut(animDuration)
            rotate(0f, -animRotation)
        }

        // items
        for (itemBinding in itemBindings) {
            itemBinding.fabMenuItem.hide()
            animators += itemBinding.root.crossfadeOut(animDuration)
        }

        // overlay
        animators += overlayBinding.root.crossfadeOut(animDuration)

        isMenuOpen = false
    }

    private fun View.rotate(from: Float, to: Float) {
        ObjectAnimator.ofFloat(this, "rotation", from, to).apply {
            duration = animDuration
            start()
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun FloatingActionButton.animateFab(@ColorInt fromColor: Int, @ColorInt toColor: Int) {
        ObjectAnimator.ofInt(this, "backgroundTint", fromColor, toColor).apply {
            duration = animDuration
            setEvaluator(ArgbEvaluator())

            addUpdateListener { animator: ValueAnimator ->
                backgroundTint = animator.animatedValue as Int
            }

            start()
        }
    }
}

fun Drawable.toBitmapDrawable() = this as BitmapDrawable

fun Drawable.toTransitionDrawable() = this as TransitionDrawable

fun Drawable.toAnimationDrawable() = this as AnimationDrawable

fun View.animateCompat() = ViewCompat.animate(this)

fun View.crossfadeIn(
    duration: Long,
    toAlpha: Float = 1f,
    listener: ((Animator?) -> Unit)? = null
): ViewPropertyAnimator {
    alpha = 0f
    isVisible = true
    return animate().apply {
        alpha(toAlpha)
        setDuration(duration)
        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                listener?.invoke(animation)
            }

            override fun onAnimationCancel(animation: Animator?) {
                listener?.invoke(animation)
            }
        })
        start()
    }
}

fun View.crossfadeOut(
    duration: Long,
    listener: ((Animator?) -> Unit)? = null
): ViewPropertyAnimator = animate().apply {
    alpha(0f)
    setDuration(duration)
    setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            isVisible = false
            listener?.invoke(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {
            isVisible = false
            listener?.invoke(animation)
        }
    })
    start()
}
