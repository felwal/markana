package com.felwal.markana.ui.setting

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.felwal.markana.R
import com.felwal.markana.databinding.ItemSettingsHeaderBinding
import com.felwal.markana.databinding.ItemSettingsSwitchBinding
import com.felwal.markana.databinding.ItemSettingsTextBinding
import com.felwal.markana.dialog.BaseDialog
import com.felwal.markana.dialog.NO_RES
import com.felwal.markana.dialog.binaryDialog
import com.felwal.markana.dialog.decimalDialog
import com.felwal.markana.dialog.radioDialog
import com.felwal.markana.dialog.textDialog
import com.felwal.markana.dialog.unaryDialog
import com.felwal.markana.util.dp
import com.felwal.markana.util.enableRipple
import com.felwal.markana.util.hideOrRemove
import com.felwal.markana.util.setTextRemoveIfEmpty
import com.felwal.markana.util.showOrHide

abstract class AbsSettingsActivity(
    private val dividerMode: DividerMode,
    private val indentEverything: Boolean
) : AppCompatActivity() {

    enum class DividerMode {
        ALWAYS,
        NEVER,
        AFTER_SECTION,
        IN_SECTION
    }

    abstract val ll: LinearLayout

    // inflate

    /**
     * Call [inflateSections] here.
     */
    protected abstract fun inflateViews()

    protected fun reflateViews() {
        ll.removeAllViews()
        inflateViews()
    }

    protected fun inflateSections(vararg sections: ItemSection) {
        for ((index, section) in sections.withIndex()) {
            section.inflate(index == sections.size - 1)
        }
    }

    // inflate item

    private fun inflateHeader(title: String) {
        val itemBinding = ItemSettingsHeaderBinding.inflate(layoutInflater, ll, true)
        itemBinding.tv.text = title

        // set params depending on [indentEverything]
        if (indentEverything) {
            itemBinding.tv.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // parent to iv + iv width + iv to tv
                marginStart = resources.getDimension(R.dimen.spacing_small).toInt() + 24 +
                    resources.getDimension(R.dimen.spacing_large).toInt()
            }
        }
    }

    protected fun inflateDialogItem(
        title: String,
        value: String,
        hideDivider: Boolean,
        @DrawableRes iconRes: Int,
        dialog: BaseDialog
    ) {
        val itemBinding = inflateTextView(title, value, hideDivider, iconRes)
        itemBinding.root.setOnClickListener {
            dialog.show(supportFragmentManager)
        }
    }

    protected fun inflateSwitchItem(
        title: String,
        descOn: String,
        descOff: String,
        checked: Boolean,
        hideDivider: Boolean,
        @DrawableRes iconRes: Int,
        onSwitch: (checked: Boolean) -> Unit
    ) {
        val desc = if (checked) descOn else descOff
        val itemBinding = inflateSwitchView(title, desc, hideDivider, iconRes)
        itemBinding.sw.isChecked = checked

        itemBinding.root.setOnClickListener {
            itemBinding.sw.isChecked = !itemBinding.sw.isChecked
            onSwitch(itemBinding.sw.isChecked)

            // update desc
            val newDesc = if (itemBinding.sw.isChecked) descOn else descOff
            itemBinding.tvDesc.setTextRemoveIfEmpty(newDesc)
        }
    }

    protected fun inflateClickItem(
        title: String,
        value: String,
        hideDivider: Boolean,
        @DrawableRes iconRes: Int,
        listener: View.OnClickListener?
    ) {
        val itemBinding = inflateTextView(title, value, hideDivider, iconRes)
        itemBinding.root.setOnClickListener(listener)
    }

    // inflate view

    private fun inflateTextView(
        title: String,
        value: String,
        hideDivider: Boolean,
        @DrawableRes iconRes: Int
    ): ItemSettingsTextBinding {
        val itemBinding = ItemSettingsTextBinding.inflate(layoutInflater, ll, true)

        // text
        itemBinding.tvTitle.text = title
        itemBinding.tvValue.setTextRemoveIfEmpty(value)

        // view
        itemBinding.root.enableRipple(this)
        itemBinding.vDivider.showOrHide(!hideDivider)

        // icon
        if (iconRes != NO_RES) {
            val icon = AppCompatResources.getDrawable(this, iconRes)
            itemBinding.ivIcon.setImageDrawable(icon)
        }
        else itemBinding.ivIcon.hideOrRemove(indentEverything)

        return itemBinding
    }

    private fun inflateSwitchView(
        title: String,
        desc: String,
        hideDivider: Boolean,
        @DrawableRes iconRes: Int
    ): ItemSettingsSwitchBinding {
        val itemBinding = ItemSettingsSwitchBinding.inflate(layoutInflater, ll, true)

        // text
        itemBinding.tvTitle.text = title
        itemBinding.tvDesc.setTextRemoveIfEmpty(desc)

        // view
        itemBinding.root.enableRipple(this)
        itemBinding.vDivider.showOrHide(!hideDivider)

        // icon
        if (iconRes != NO_RES) {
            val icon = AppCompatResources.getDrawable(this, iconRes)
            itemBinding.ivIcon.setImageDrawable(icon)
        }
        else itemBinding.ivIcon.hideOrRemove(indentEverything)

        return itemBinding
    }

    // item

    protected inner class ItemSection(
        val title: String,
        private vararg val items: SettingItem
    ) {

        fun inflate(lastSection: Boolean) {
            inflateHeader(title)

            for ((index, item) in items.withIndex()) {
                val lastItem = index == items.size - 1
                item.inflate(
                    if (lastSection && lastItem) true
                    else when (dividerMode) {
                        DividerMode.ALWAYS -> false
                        DividerMode.NEVER -> true
                        DividerMode.IN_SECTION -> lastItem
                        DividerMode.AFTER_SECTION -> !lastItem
                    }
                )
            }
        }
    }

    protected inner class BooleanItem(
        title: String,
        val descOn: String = "",
        val descOff: String = descOn,
        val value: Boolean,
        @DrawableRes iconRes: Int = NO_RES,
        val onSwitch: (checked: Boolean) -> Unit
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateSwitchItem(title, descOn, descOff, value, hideDivider, iconRes, onSwitch)
        }
    }

    protected inner class StringItem(
        title: String,
        val desc: String = "",
        val value: String,
        val hint: String,
        @DrawableRes iconRes: Int = NO_RES,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, value, hideDivider, iconRes,
                textDialog(title, desc, value, hint, R.string.dialog_btn_set, tag)
            )
        }
    }

    protected inner class FloatItem(
        title: String,
        val desc: String = "",
        val value: Float,
        val hint: String,
        @DrawableRes iconRes: Int = NO_RES,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, value.toString(), hideDivider, iconRes,
                decimalDialog(title, desc, value, hint, R.string.dialog_btn_set, tag)
            )
        }
    }

    protected inner class SingleSelectionItem(
        title: String,
        val desc: String = "",
        val values: List<String>,
        val selectedIndex: Int,
        @DrawableRes iconRes: Int = NO_RES,
        val tag: String
    ) : SettingItem(title, iconRes) {

        val value: String get() = values[selectedIndex]

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, value, hideDivider, iconRes,
                radioDialog(title, desc, values, selectedIndex, tag)
            )
        }
    }

    protected inner class ActionItem(
        title: String,
        val desc: String = "",
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes val dialogPosBtnRes: Int,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, "", hideDivider, iconRes,
                binaryDialog(title, desc, dialogPosBtnRes, tag)
            )
        }
    }

    protected inner class InfoItem(
        title: String,
        val desc: String = "",
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes val dialogBtnRes: Int,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, "", hideDivider, iconRes,
                unaryDialog(title, desc, dialogBtnRes, tag)
            )
        }
    }

    protected inner class SubSettingsItem(
        title: String,
        val desc: String = "",
        @DrawableRes iconRes: Int = NO_RES,
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateClickItem(title, desc, hideDivider, iconRes) {
                //launchActivity<>()
            }
        }
    }

    abstract inner class SettingItem(
        val title: String,
        @DrawableRes val iconRes: Int
    ) {
        abstract fun inflate(hideDivider: Boolean)
    }
}