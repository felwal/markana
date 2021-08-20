package com.felwal.markana.widget.dialog

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.markana.R
import com.felwal.markana.util.firsts
import com.felwal.markana.util.orEmpty
import com.felwal.markana.util.seconds

private const val ARG_ITEMS = "items"
private const val ARG_CHECKED_ITEMS = "checkedItems"

class CheckDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
    private lateinit var items: Array<out String>
    private lateinit var checkedItems: BooleanArray

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
            items = getStringArray(ARG_ITEMS).orEmpty()
            checkedItems = getBooleanArray(ARG_CHECKED_ITEMS).orEmpty()
        }
    }

    override fun buildDialog(): AlertDialog = builder.run {
        setTitle(title)

        setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
            checkedItems[which] = isChecked
        }
        setPositiveButton(posBtnTxtRes) { _, _ ->
            listener.onCheckDialogPositiveClick(checkedItems, dialogTag)
        }
        setCancelButton(negBtnTxtRes)

        show()
    }

    //

    interface DialogListener {
        fun onCheckDialogPositiveClick(checkedItems: BooleanArray, tag: String)
    }
}

fun checkDialog(
    title: String,
    message: String = "",
    vararg items: Pair<String, Boolean>,
    @StringRes posBtnTxtRes: Int = R.string.dialog_btn_ok,
    @StringRes negBtnTxtRes: Int = R.string.dialog_btn_cancel,
    tag: String
): CheckDialog = CheckDialog().apply {
    arguments = putBaseBundle(title, message, posBtnTxtRes, negBtnTxtRes, tag).apply {
        putStringArray(ARG_ITEMS, items.firsts.toTypedArray())
        putBooleanArray(ARG_CHECKED_ITEMS, items.seconds.toBooleanArray())
    }
}