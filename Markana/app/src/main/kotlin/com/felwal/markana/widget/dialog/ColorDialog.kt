package com.felwal.markana.widget.dialog

import android.content.Context
import android.os.Bundle
import android.widget.TableRow
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.markana.R
import com.felwal.markana.databinding.DialogColorBinding
import com.felwal.markana.databinding.ItemDialogColorBinding
import com.felwal.markana.util.backgroundTint
import com.felwal.markana.util.clamp
import com.felwal.markana.util.getColorAttr
import com.felwal.markana.util.getDrawableCompat
import com.felwal.markana.util.isPortrait
import com.felwal.markana.util.orEmpty

private const val ARG_ITEMS = "items"
private const val ARG_CHECKED_ITEM = "checkedItem"

private const val COLUMN_COUNT_PORTRAIT = 4
private const val COLUMN_COUNT_LANDSCAPE = 5

class ColorDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
    @ColorInt private lateinit var items: IntArray
    private var checkedItem = 0

    // DialogFragment

    override fun onAttach(c: Context) {
        super.onAttach(c)

        listener = try {
            c as DialogListener
        }
        catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement DialogListener")
        }
    }

    // BaseDialog

    override fun unpackBundle(bundle: Bundle?) {
        bundle?.apply {
            items = getIntArray(ARG_ITEMS).orEmpty()
            checkedItem = getInt(ARG_CHECKED_ITEM, 0).clamp(-1, items.size)
        }
    }

    override fun buildDialog(): AlertDialog = builder.run {
        val binding = DialogColorBinding.inflate(inflater)
        setView(binding.root)

        val columnCount = if (context.isPortrait) COLUMN_COUNT_PORTRAIT else COLUMN_COUNT_LANDSCAPE

        var tr = TableRow(binding.tl.context)
        binding.tl.addView(tr)

        for ((i, color) in items.withIndex()) {
            // inflate row
            if (i != 0 && i % columnCount == 0) {
                tr = TableRow(context)
                binding.tl.addView(tr)
            }

            // inflate item
            val itemBinding = ItemDialogColorBinding.inflate(inflater, tr, false)
            itemBinding.iv.backgroundTint = color

            // set checked drawable
            if (i == checkedItem) {
                val icon = context.getDrawableCompat(R.drawable.ic_check_24)
                icon?.setTint(context.getColorAttr(R.attr.colorSurface))
                itemBinding.iv.setImageDrawable(icon)
            }

            itemBinding.iv.setOnClickListener {
                listener.onColorDialogItemClick(i, dialogTag)
                dialog?.cancel()
            }

            tr.addView(itemBinding.root)
        }

        setTitle(title)
        if (message != "") setMessage(message)

        setCancelButton(negBtnTxtRes)

        show()
    }

    //

    interface DialogListener {
        fun onColorDialogItemClick(checkedItem: Int, tag: String)
    }
}

fun colorDialog(
    title: String,
    message: String = "",
    @ColorInt items: IntArray,
    checkedItem: Int? = null,
    @StringRes negBtnTxtRes: Int = R.string.dialog_btn_cancel,
    tag: String
): ColorDialog = ColorDialog().apply {
    arguments = putBaseBundle(title, message, NO_RES, negBtnTxtRes = negBtnTxtRes, tag = tag).apply {
        putIntArray(ARG_ITEMS, items)
        putInt(ARG_CHECKED_ITEM, checkedItem ?: NULL_INT)
    }
}