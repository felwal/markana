package com.felwal.markana.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import com.felwal.markana.R

private const val BUNDLE_PASS_VALUE = "passValue"

private const val TAG_DEFAULT = "binaryDialog"

class UnaryDialog : BaseDialog() {

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
                listener.onUnaryDialogClick(passValue, dialogTag)
            }

        return builder.show()
    }

    //

    interface DialogListener {
        fun onUnaryDialogClick(passValue: String?, tag: String)
    }
}

fun unaryDialog(
    @StringRes titleRes: Int = R.string.dialog_title_continue,
    @StringRes messageRes: Int = NO_RES,
    @StringRes btnTxtRes: Int = R.string.dialog_btn_continue,
    tag: String,
    passValue: String? = null
): UnaryDialog {
    val instance = UnaryDialog()
    val bundle: Bundle = putBaseBundle(titleRes, messageRes, btnTxtRes, tag)

    bundle.putString(BUNDLE_PASS_VALUE, passValue)

    instance.arguments = bundle
    return instance
}