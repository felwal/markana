package com.felwal.markana.ui.setting

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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
import com.felwal.markana.util.enableRipple
import com.felwal.markana.util.remove
import com.felwal.markana.util.setTextRemoveIfEmpty
import com.felwal.markana.util.showOrHide

abstract class AbsSettingsActivity(
    private val dividerMode: DividerMode
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
        itemBinding.tvSettingsItemHeaderTitle.text = title
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
        desc: String = "",
        checked: Boolean,
        hideDivider: Boolean,
        @DrawableRes iconRes: Int,
        onSwitch: (checked: Boolean) -> Unit
    ) {
        val itemBinding = inflateSwitchView(title, desc, hideDivider, iconRes)
        itemBinding.sw.isChecked = checked

        itemBinding.root.setOnClickListener {
            itemBinding.sw.isChecked = !itemBinding.sw.isChecked
            onSwitch(itemBinding.sw.isChecked)
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
        else itemBinding.ivIcon.remove()

        return itemBinding
    }

    private fun inflateSwitchView(
        title: String,
        desc: String = "",
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
        else itemBinding.ivIcon.remove()

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
        val desc: String = "",
        val value: Boolean,
        @DrawableRes iconRes: Int = NO_RES,
        val onSwitch: (checked: Boolean) -> Unit
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateSwitchItem(title, desc, value, hideDivider, iconRes, onSwitch)
        }
    }

    protected inner class StringItem(
        title: String,
        val value: String,
        val dialogHint: String,
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes val dialogTitleRes: Int,
        @StringRes val dialogMsgRes: Int = NO_RES,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, value, hideDivider, iconRes,
                textDialog(dialogTitleRes, dialogMsgRes, value, dialogHint, R.string.dialog_btn_set, tag)
            )
        }
    }

    protected inner class FloatItem(
        title: String,
        val value: Float,
        val dialogHint: String,
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes val dialogTitleRes: Int,
        @StringRes val dialogMsgRes: Int = NO_RES,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, value.toString(), hideDivider, iconRes,
                decimalDialog(dialogTitleRes, dialogMsgRes, value, dialogHint, R.string.dialog_btn_set, tag)
            )
        }
    }

    protected inner class SingleSelectionItem(
        title: String,
        val list: List<String>,
        val selectedIndex: Int,
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes val dialogTitleRes: Int,
        @StringRes val dialogMsgRes: Int = NO_RES,
        val tag: String
    ) : SettingItem(title, iconRes) {

        val value: String get() = list[selectedIndex]

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, value, hideDivider, iconRes,
                radioDialog(dialogTitleRes, dialogMsgRes, list, selectedIndex, tag)
            )
        }
    }

    protected inner class ActionItem(
        title: String,
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes val dialogTitleRes: Int,
        @StringRes val dialogMsgRes: Int = NO_RES,
        @StringRes val dialogPosBtnRes: Int,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, "", hideDivider, iconRes,
                binaryDialog(dialogTitleRes, dialogMsgRes, dialogPosBtnRes, tag)
            )
        }
    }

    protected inner class InfoItem(
        title: String,
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes val dialogTitleRes: Int,
        @StringRes val dialogMsgRes: Int = NO_RES,
        @StringRes val dialogBtnRes: Int,
        val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, "", hideDivider, iconRes,
                unaryDialog(dialogTitleRes, dialogMsgRes, dialogBtnRes, tag)
            )
        }
    }

    protected inner class SubSettingsItem(
        title: String,
        @DrawableRes iconRes: Int = NO_RES,
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateClickItem(title, "...", hideDivider, iconRes) {
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