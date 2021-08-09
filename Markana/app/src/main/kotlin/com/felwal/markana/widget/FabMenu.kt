package com.felwal.markana.widget

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.felwal.markana.R
import com.felwal.markana.databinding.ItemFabMenuBinding
import com.felwal.markana.databinding.ItemFabMenuItemBinding
import com.felwal.markana.databinding.ItemFabMenuOverlayBinding
import com.felwal.markana.util.ANIM_DURATION
import com.felwal.markana.util.crossfadeIn
import com.felwal.markana.util.crossfadeOut
import com.felwal.markana.util.getColorByAttr
import com.felwal.markana.util.getDrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

const val OVERLAY_ALPHA = 0.96f

class FabMenu(
    private val context: Context,
    private val inflater: LayoutInflater,
    parent: ViewGroup
) {

    private val binding = ItemFabMenuBinding.inflate(inflater, parent, true)
    private val overlayBinding = ItemFabMenuOverlayBinding.inflate(inflater, parent, true)
    private var itemBindings: MutableList<ItemFabMenuItemBinding> = mutableListOf()

    private var isFabMenuOpen: Boolean = false

    init {
        // open/close
        binding.fab.setOnClickListener {
            if (isFabMenuOpen) closeMenu() else openMenu()
        }

        // close
        overlayBinding.root.setOnClickListener { closeMenu() }
    }

    fun showHideOnScroll(dy: Int) {
        if (binding.fab.isOrWillBeShown && dy > 0) binding.fab.hide()
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

    private fun openMenu() {
        animateFab()

        for (itemBinding in itemBindings) {
            itemBinding.fabMenuItem.show()
            itemBinding.root.crossfadeIn()
        }
        overlayBinding.root.crossfadeIn(OVERLAY_ALPHA)

        isFabMenuOpen = true
    }

    private fun closeMenu() {
        animateFab()

        for (itemBinding in itemBindings) {
            itemBinding.fabMenuItem.hide()
            itemBinding.root.crossfadeOut()
        }
        overlayBinding.root.crossfadeOut()

        isFabMenuOpen = false
    }

    fun closeMenuIfOpen() = isFabMenuOpen.also {
        if (isFabMenuOpen) closeMenu()
    }

    private fun animateFab() {
        @ColorInt val closedColor: Int = context.getColorByAttr(R.attr.colorSecondary)
        @ColorInt val openColor: Int = context.getColorByAttr(R.attr.colorSurface)

        @ColorInt val fromColor: Int
        @ColorInt val toColor: Int
        val toIcon: Drawable?

        // animate to closed menu
        if (isFabMenuOpen) {
            fromColor = openColor
            toColor = closedColor
            toIcon = context.getDrawableCompat(R.drawable.ic_add)?.mutate()
            toIcon?.setColorFilter(context.getColorByAttr(R.attr.colorOnSecondary), PorterDuff.Mode.SRC_IN)
        }
        // animate to open menu
        else {
            fromColor = closedColor
            toColor = openColor
            toIcon = context.getDrawableCompat(R.drawable.ic_clear)?.mutate()
            toIcon?.setColorFilter(context.getColorByAttr(R.attr.colorControlActivated), PorterDuff.Mode.SRC_IN)
        }

        binding.fab.animateFab(fromColor, toColor, toIcon)
    }
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