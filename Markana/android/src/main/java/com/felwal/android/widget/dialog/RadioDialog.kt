package com.felwal.android.widget.dialog

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.android.R

private const val ARG_ITEMS = "items"
private const val ARG_CHECKED_ITEM = "checkedItem"

class RadioDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
    private lateinit var items: Array<out String>
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
            items = getStringArray(ARG_ITEMS).orEmpty()
            checkedItem = getInt(ARG_CHECKED_ITEM, 0).coerceIn(0, items.size)
        }
    }

    override fun buildDialog(): AlertDialog = builder.run {
        setTitle(title)
        if (message != "") setMessage(message)

        setSingleChoiceItems(items, checkedItem) { dialog, which ->
            dialog.cancel()
            listener.onRadioDialogItemClick(which, dialogTag)
        }
        setCancelButton(negBtnTxtRes)

        show()
    }

    //

    interface DialogListener {
        fun onRadioDialogItemClick(checkedItem: Int, tag: String)
    }

    //

    companion object {
        @JvmStatic
        fun newInstance(
            title: String,
            message: String = "",
            items: List<String>,
            checkedItem: Int,
            @StringRes negBtnTxtRes: Int = R.string.dialog_btn_cancel,
            tag: String
        ): RadioDialog = RadioDialog().apply {
            arguments = putBaseBundle(title, message, NO_RES, negBtnTxtRes = negBtnTxtRes, tag = tag).apply {
                putStringArray(ARG_ITEMS, items.toTypedArray())
                putInt(ARG_CHECKED_ITEM, checkedItem)
            }
        }
    }
}

fun radioDialog(
    title: String,
    message: String = "",
    items: List<String>,
    checkedItem: Int,
    @StringRes negBtnTxtRes: Int = R.string.dialog_btn_cancel,
    tag: String
): RadioDialog = RadioDialog.newInstance(title, message, items, checkedItem, negBtnTxtRes, tag)