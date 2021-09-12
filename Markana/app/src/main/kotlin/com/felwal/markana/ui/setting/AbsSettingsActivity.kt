package com.felwal.markana.ui.setting

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import com.felwal.markana.R
import com.felwal.markana.databinding.ItemSettingsHeaderBinding
import com.felwal.markana.databinding.ItemSettingsSwitchBinding
import com.felwal.markana.databinding.ItemSettingsTextBinding
import com.felwal.markana.util.enableRipple
import com.felwal.markana.util.getDimension
import com.felwal.markana.util.getDrawableCompat
import com.felwal.markana.util.hideOrRemove
import com.felwal.markana.util.setTextRemoveIfEmpty
import com.felwal.markana.widget.dialog.BaseDialog
import com.felwal.markana.widget.dialog.NO_RES
import com.felwal.markana.widget.dialog.binaryDialog
import com.felwal.markana.widget.dialog.decimalDialog
import com.felwal.markana.widget.dialog.numberDialog
import com.felwal.markana.widget.dialog.radioDialog
import com.felwal.markana.widget.dialog.textDialog
import com.felwal.markana.widget.dialog.unaryDialog

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

    abstract val llItemContainer: LinearLayout

    // inflate

    /**
     * Call [inflateSections] here.
     */
    protected abstract fun inflateViews()

    protected fun reflateViews() {
        llItemContainer.removeAllViews()
        inflateViews()
    }

    protected fun inflateSections(vararg sections: ItemSection) {
        for ((index, section) in sections.withIndex()) {
            section.inflate(lastSection = index == sections.size - 1)
        }
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
        private val descOn: String = "",
        private val descOff: String = descOn,
        private val value: Boolean,
        @DrawableRes iconRes: Int = NO_RES,
        private val onSwitch: (checked: Boolean) -> Unit
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateSwitchItem(title, descOn, descOff, value, hideDivider, iconRes, onSwitch)
        }
    }

    protected inner class StringItem(
        title: String,
        private val desc: String? = null,
        private val msg: String = "",
        private val value: String,
        private val hint: String,
        @DrawableRes iconRes: Int = NO_RES,
        private val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, desc ?: value, hideDivider, iconRes,
                textDialog(
                    title = title, message = msg,
                    text = value, hint = hint,
                    posBtnTxtRes = R.string.dialog_btn_set,
                    tag = tag
                )
            )
        }
    }

    protected inner class IntItem(
        title: String,
        private val desc: String? = null,
        private val msg: String = "",
        private val value: Int,
        private val hint: String,
        @DrawableRes iconRes: Int = NO_RES,
        private val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, desc ?: value.toString(), hideDivider, iconRes,
                numberDialog(
                    title = title, message = msg,
                    text = value, hint = hint,
                    posBtnTxtRes = R.string.dialog_btn_set,
                    tag = tag
                )
            )
        }
    }

    protected inner class FloatItem(
        title: String,
        private val desc: String? = null,
        private val msg: String = "",
        private val value: Float,
        private val hint: String,
        @DrawableRes iconRes: Int = NO_RES,
        private val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, desc ?: value.toString(), hideDivider, iconRes,
                decimalDialog(
                    title = title, message = msg,
                    text = value, hint = hint,
                    posBtnTxtRes = R.string.dialog_btn_set,
                    tag = tag
                )
            )
        }
    }

    protected inner class SingleSelectionItem(
        title: String,
        private val desc: String? = null,
        private val msg: String = "",
        private val values: List<String>,
        private val selectedIndex: Int,
        @DrawableRes iconRes: Int = NO_RES,
        private val tag: String
    ) : SettingItem(title, iconRes) {

        val value: String get() = values[selectedIndex]

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, desc ?: value, hideDivider, iconRes,
                radioDialog(
                    title = title, message = msg,
                    items = values, checkedItem = selectedIndex,
                    tag = tag
                )
            )
        }
    }

    protected inner class ConfirmationItem(
        title: String,
        private val desc: String = "",
        private val msg: String = "",
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes private val dialogPosBtnRes: Int,
        private val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, desc, hideDivider, iconRes,
                binaryDialog(
                    title = title, message = msg,
                    posBtnTxtRes = dialogPosBtnRes,
                    tag = tag
                )
            )
        }
    }

    protected inner class InfoItem(
        title: String,
        private val desc: String = "",
        private val msg: String = "",
        @DrawableRes iconRes: Int = NO_RES,
        @StringRes private val dialogBtnRes: Int,
        private val tag: String
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateDialogItem(
                title, desc, hideDivider, iconRes,
                unaryDialog(title, msg, dialogBtnRes, tag)
            )
        }
    }

    protected inner class ActionItem(
        title: String,
        private val desc: String = "",
        @DrawableRes iconRes: Int = NO_RES,
        private val onClick: () -> Unit
    ) : SettingItem(title, iconRes) {

        override fun inflate(hideDivider: Boolean) {
            inflateClickItem(title, desc, hideDivider, iconRes) {
                onClick()
            }
        }
    }

    abstract inner class SettingItem(
        val title: String,
        @DrawableRes val iconRes: Int
    ) {
        abstract fun inflate(hideDivider: Boolean)
    }

    // inflate item

    private fun inflateHeader(title: String) {
        val itemBinding = ItemSettingsHeaderBinding.inflate(layoutInflater, llItemContainer, true)
        itemBinding.tv.text = title

        // set start margin
        if (indentEverything) {
            itemBinding.tv.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // parent to iv + iv width + iv to tv
                marginStart = getDimension(R.dimen.spacing_small).toInt() + 24 +
                    getDimension(R.dimen.spacing_large).toInt()
            }
        }
    }

    private fun inflateDialogItem(
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

    private fun inflateSwitchItem(
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

    private fun inflateClickItem(
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
    ): ItemSettingsTextBinding =
        ItemSettingsTextBinding.inflate(layoutInflater, llItemContainer, true).apply {
            // text
            tvTitle.text = title
            tvValue.setTextRemoveIfEmpty(value)

            // view
            root.enableRipple(this@AbsSettingsActivity)
            vDivider.isInvisible = hideDivider

            // icon
            if (iconRes != NO_RES) {
                val icon = getDrawableCompat(iconRes)
                ivIcon.setImageDrawable(icon)
            }
            else ivIcon.hideOrRemove(indentEverything)
        }

    private fun inflateSwitchView(
        title: String,
        desc: String,
        hideDivider: Boolean,
        @DrawableRes iconRes: Int
    ): ItemSettingsSwitchBinding =
        ItemSettingsSwitchBinding.inflate(layoutInflater, llItemContainer, true).apply {

            // text
            tvTitle.text = title
            tvDesc.setTextRemoveIfEmpty(desc)

            // view
            root.enableRipple(this@AbsSettingsActivity)
            vDivider.isInvisible = hideDivider

            // icon
            if (iconRes != NO_RES) {
                val icon = getDrawableCompat(iconRes)
                ivIcon.setImageDrawable(icon)
            }
            else ivIcon.hideOrRemove(indentEverything)
        }
}