package com.felwal.markana.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import com.felwal.markana.R

private const val ARG_PASS_VALUE = "passValue"

private const val TAG_DEFAULT = "binaryDialog"

class UnaryDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
    private var passValue: String? = null

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
            passValue = getString(ARG_PASS_VALUE, null)
        }
    }

    override fun buildDialog(): AlertDialog = builder.run {
        setTitle(title)
        if (message != "") setMessage(message)

        setPositiveButton(posBtnTxtRes) { _, _ ->
            listener.onUnaryDialogClick(passValue, dialogTag)
        }

        show()
    }

    //

    interface DialogListener {
        fun onUnaryDialogClick(passValue: String?, tag: String)
    }
}

fun unaryDialog(
    title: String, message: String = "",
    @StringRes btnTxtRes: Int = R.string.dialog_btn_ok,
    tag: String,
    passValue: String? = null
): UnaryDialog = UnaryDialog().apply {
    arguments = putBaseBundle(title, message, btnTxtRes, tag).apply {
        putString(ARG_PASS_VALUE, passValue)
    }
}