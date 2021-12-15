package com.felwal.markana.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.felwal.android.util.backgroundTint
import com.felwal.android.util.getColorByAttr
import com.felwal.android.util.getDrawableCompat
import com.felwal.markana.R
import com.felwal.markana.databinding.ItemFloatingactionmenuMiniBinding
import com.felwal.markana.databinding.ItemFloatingactionmenuOverlayBinding
import com.felwal.markana.databinding.ViewFloatingactionmenuBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun View.getActivity(): Activity? {
    var c = context
    while (c is ContextWrapper) {
        if (c is Activity)  return c
        c = c.baseContext
    }
    return null
}

private fun View.getChildAt(index: Int): View? = (this as? ViewGroup)?.getChildAt(index)

val Activity.contentView: View? get() = window.decorView.rootView.rootView
    .getChildAt(0)
    ?.getChildAt(1)
    ?.getChildAt(0)
    ?.getChildAt(1)
    ?.getChildAt(0)

class FloatingActionMenu(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    var isMenuOpen = false

    private val animators: MutableList<ViewPropertyAnimator> = mutableListOf()

    private val inflater = context.layoutInflater

    private var binding = ViewFloatingactionmenuBinding.inflate(inflater, this, true)
    private var itemBindings: MutableList<ItemFloatingactionmenuMiniBinding> = mutableListOf()
    private lateinit var overlayBinding: ItemFloatingactionmenuOverlayBinding

    val fab get() = binding.fab
    val overlay get() = overlayBinding.root
    val miniFabs get() = itemBindings.map { it.fabMenuItem }
    val closedImageView get() = binding.ivFabClosedIcon
    val openedImageView get() = binding.ivFabOpenedIcon

    @ColorInt private var closedFabColor: Int
    @ColorInt private var openedFabColor: Int
    @ColorInt private var miniFabColor: Int

    @ColorInt private var closedIconTint: Int
    @ColorInt private var openedIconTint: Int
    @ColorInt private var miniIconTint: Int

    private var closedIconSrc: Drawable?
    private var openedIconSrc: Drawable?

    private var overlayAlpha: Float
    private var animDuration: Long
    private var animRotation: Float

    private var firstMenuItemAsMainFab: Boolean

    //

    init {
        // get attrs
        context.theme.obtainStyledAttributes(attrs, R.styleable.FloatingActionMenu, 0, 0).apply {
            try {
                // container colors
                closedFabColor = getColor(
                    R.styleable.FloatingActionMenu_closedFabColor,
                    context.getColorByAttr(R.attr.colorSecondary)
                )
                openedFabColor = getColor(
                    R.styleable.FloatingActionMenu_openedFabColor,
                    context.getColorByAttr(R.attr.colorSurface)
                )
                miniFabColor = getColor(
                    R.styleable.FloatingActionMenu_miniFabColor,
                    context.getColorByAttr(R.attr.colorSecondary)
                )

                // icon tints
                closedIconTint = getColor(
                    R.styleable.FloatingActionMenu_closedIconTint,
                    context.getColorByAttr(R.attr.colorOnSecondary)
                )
                openedIconTint = getColor(
                    R.styleable.FloatingActionMenu_openedIconTint,
                    context.getColorByAttr(R.attr.colorOnSurface)
                )
                miniIconTint = getColor(
                    R.styleable.FloatingActionMenu_miniIconTint,
                    context.getColorByAttr(R.attr.colorOnSecondary)
                )

                // icon drawables
                closedIconSrc = getDrawable(R.styleable.FloatingActionMenu_closedIconSrc)
                    ?: context.getDrawableCompat(R.drawable.ic_add_24)
                openedIconSrc = getDrawable(R.styleable.FloatingActionMenu_openedIconSrc)
                    ?: context.getDrawableCompat(R.drawable.ic_close_24)

                //
                overlayAlpha = getFloat(R.styleable.FloatingActionMenu_overlayAlpha, 96f)
                animDuration = getInt(R.styleable.FloatingActionMenu_animDuration, 150).toLong()
                animRotation = getFloat(R.styleable.FloatingActionMenu_animRotation, 135f)

                //
                firstMenuItemAsMainFab = getBoolean(R.styleable.FloatingActionMenu_firstMenuItemAsMainFab, false)
            }
            finally {
                recycle()
            }
        }

        //
        closedImageView.isVisible = false
        openedImageView.isVisible = false

        // open/close
        fab.setOnClickListener { toggleMenu() }
    }

    /**
     * Inflates and initiates the overlay. Must be called after [Activity.setContentView].
     */
    fun onSetContentView() {
        val contentView = getActivity()!!.contentView!! as ViewGroup
        overlayBinding = ItemFloatingactionmenuOverlayBinding.inflate(inflater, contentView, true)

        // close on outside click
        overlay.setOnClickListener { closeMenu() }
    }

    //

    fun updateVisibilityOnScroll(dy: Int) {
        // prevent the menu from staying open while the fab is being hidden
        if (!isMenuOpen && fab.isOrWillBeShown && dy > 0) fab.hide()
        else if (fab.isOrWillBeHidden && dy < 0) fab.show()
    }

    fun addItem(title: String, icon: Drawable?, onClick: (View) -> Unit) {
        val itemBinding = ItemFloatingactionmenuMiniBinding.inflate(inflater, binding.root, false)
        // neccessary for getting the right index
        binding.root.addView(itemBinding.root, 0)

        itemBinding.tvMenuItemTitle.text = title
        itemBinding.fabMenuItem.setImageDrawable(icon)
        itemBinding.fabMenuItem.setOnClickListener(onClick)

        itemBindings.add(itemBinding)
    }

    //

    fun toggleMenu() = if (isMenuOpen) closeMenu() else openMenu()

    fun openMenu() {
        // dont open the menu while the fab is hiding
        if (fab.isOrWillBeHidden) return
        isMenuOpen = true

        animators.cancelAndClear()

        // overlay
        animators += overlay.crossfadeIn(animDuration, overlayAlpha)

        // container
        fab.animateFab(closedFabColor, openedFabColor)

        // icons
        closedImageView.apply {
            rotate(0f, animRotation)
            isVisible = true
            // remove fab icon, as we cannot rotate it (since that rotates the container as well)
            fab.setImageDrawable(null)
            animators += crossfadeOut(animDuration)
        }
        openedImageView.apply {
            animators += crossfadeIn(animDuration)
            rotate(-animRotation, 0f)
        }

        // items
        for (itemBinding in itemBindings) {
            itemBinding.fabMenuItem.show()
            animators += itemBinding.root.crossfadeIn(animDuration)
        }
    }

    fun closeMenu() {
        isMenuOpen = false

        animators.cancelAndClear()

        // overlay
        animators += overlay.crossfadeOut(animDuration)

        // container
        fab.animateFab(openedFabColor, closedFabColor)

        // icons
        closedImageView.apply {
            rotate(animRotation, 0f)
            animators += crossfadeIn(animDuration) {
                // readd fab icon, as we want the fab hiding animation
                fab.setImageDrawable(closedIconSrc)
                isVisible = false
            }
        }
        openedImageView.apply {
            animators += crossfadeOut(animDuration)
            rotate(0f, -animRotation)
        }

        // items
        for (itemBinding in itemBindings) {
            itemBinding.fabMenuItem.hide()
            animators += itemBinding.root.crossfadeOut(animDuration)
        }
    }

    private fun MutableList<ViewPropertyAnimator>.cancelAndClear() {
        forEach { it.cancel() }
        clear()
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

//

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

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)