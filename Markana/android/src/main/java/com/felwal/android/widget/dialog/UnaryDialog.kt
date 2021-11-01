package com.felwal.android.widget.dialog

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.android.R

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

    //

    companion object {
        @JvmStatic
        fun newInstance(
            title: String,
            message: String = "",
            @StringRes btnTxtRes: Int = R.string.dialog_btn_ok,
            tag: String
        ): UnaryDialog = UnaryDialog().apply {
            arguments = putBaseBundle(title, message, btnTxtRes, tag = tag)
        }
    }
}

fun unaryDialog(
    title: String,
    message: String = "",
    @StringRes btnTxtRes: Int = R.string.dialog_btn_ok,
    tag: String
): UnaryDialog = UnaryDialog.newInstance(title, message, btnTxtRes, tag)