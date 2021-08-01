package com.felwal.markana.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import com.felwal.markana.R
import com.felwal.markana.util.string
import com.felwal.markana.util.uiToast

private const val BUNDLE_TEXT = "text"
private const val BUNDLE_HINT = "hint"

private const val TAG_DEFAULT = "decimalDialog"

class DecimalDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // arguments
    private var text = 0f
    private lateinit var hint: String

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
            text = getFloat(BUNDLE_TEXT, 0f)
            hint = getString(BUNDLE_HINT, "")
        }
    }

    override fun buildDialog(): AlertDialog {
        val vDialog: View = inflater.inflate(R.layout.dialog_decimal, null)
        val et = vDialog.findViewById<EditText>(R.id.et_dialog_decimal)

        et.hint = hint
        if (text != NO_FLOAT_TEXT) et.setText(text.toString())
        if (!message.equals("")) builder.setMessage(message)

        builder
            .setView(vDialog)
            .setTitle(title)
            .setPositiveButton(posBtnTxtRes) { _, _ ->
                try {
                    val input = et.string.toFloat()
                    listener.onDecimalDialogPositiveClick(input, tag)
                }
                catch (e: NumberFormatException) {
                    activity?.uiToast(getString(R.string.toast_err_no_input))
                }
            }
            .setCancelButton(negBtnTxtRes)
        return builder.show()
    }

    //

    interface DialogListener {
        fun onDecimalDialogPositiveClick(input: Float, tag: String?)
    }
}

fun decimalDialog(
    @StringRes titleRes: Int = NO_RES,
    @StringRes messageRes: Int = NO_RES,
    text: Float,
    hint: String = "",
    @StringRes posBtnTxtRes: Int,
    tag: String
): DecimalDialog {
    val instance = DecimalDialog()
    val bundle: Bundle = putBaseBundle(titleRes, messageRes, posBtnTxtRes, tag)

    bundle.putFloat(BUNDLE_TEXT, text)
    bundle.putString(BUNDLE_HINT, hint)

    instance.arguments = bundle
    return instance
}