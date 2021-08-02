package com.felwal.markana.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import com.felwal.markana.R

private const val BUNDLE_PASS_VALUE = "passValue"

private const val TAG_DEFAULT = "binaryDialog"

class BinaryDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // arguments
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

    override fun unpackBundle() {
        val bundle: Bundle? = unpackBaseBundle(TAG_DEFAULT)

        bundle?.apply {
            passValue = getString(BUNDLE_PASS_VALUE, null)
        }
    }

    override fun buildDialog(): AlertDialog {
        if (message != "") builder.setMessage(message)

        builder
            .setTitle(title)
            .setPositiveButton(posBtnTxtRes) { _, _ ->
                listener.onBinaryDialogPositiveClick(passValue, dialogTag)
            }
            .setCancelButton(negBtnTxtRes)

        return builder.show()
    }

    //

    interface DialogListener {
        fun onBinaryDialogPositiveClick(passValue: String?, tag: String)
    }
}

fun binaryDialog(
    title: String,
    message: String = "",
    @StringRes posBtnTxtRes: Int = R.string.dialog_btn_ok,
    tag: String,
    passValue: String? = null
): BinaryDialog {
    val instance = BinaryDialog()
    val bundle: Bundle = putBaseBundle(title, message, posBtnTxtRes, tag)

    bundle.putString(BUNDLE_PASS_VALUE, passValue)

    instance.arguments = bundle
    return instance
}