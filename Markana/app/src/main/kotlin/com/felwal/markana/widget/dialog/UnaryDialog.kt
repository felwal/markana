package com.felwal.markana.widget.dialog

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.markana.R

private const val ARG_PASS_VALUE = "passValue"

class UnaryDialog : BaseDialog() {

    private lateinit var listener: DialogListener

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
        // bundle is empty but for base bundle
    }

    override fun buildDialog(): AlertDialog = builder.run {
        setTitle(title)
        if (message != "") setMessage(message)

        setPositiveButton(posBtnTxtRes) { _, _ ->
            listener.onUnaryDialogClick(dialogTag)
        }

        show()
    }

    //

    interface DialogListener {
        fun onUnaryDialogClick(tag: String)
    }
}

fun unaryDialog(
    title: String,
    message: String = "",
    @StringRes btnTxtRes: Int = R.string.dialog_btn_ok,
    tag: String
): UnaryDialog = UnaryDialog().apply {
    arguments = putBaseBundle(title, message, btnTxtRes, tag = tag)
}