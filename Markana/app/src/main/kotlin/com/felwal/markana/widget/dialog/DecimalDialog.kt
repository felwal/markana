package com.felwal.markana.widget.dialog

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.markana.R
import com.felwal.markana.databinding.DialogDecimalBinding
import com.felwal.markana.util.string
import com.felwal.markana.util.toast

const val NO_FLOAT_TEXT = -1f

private const val ARG_TEXT = "text"
private const val ARG_HINT = "hint"

class DecimalDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
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

    override fun unpackBundle(bundle: Bundle?) {
        bundle?.apply {
            text = getFloat(ARG_TEXT, 0f)
            hint = getString(ARG_HINT, "")
        }
    }

    override fun buildDialog(): AlertDialog {
        val binding = DialogDecimalBinding.inflate(inflater)

        binding.et.hint = hint
        if (text != NO_FLOAT_TEXT) binding.et.setText(text.toString())

        return builder.run {
            setView(binding.root)
            setTitle(title)
            if (message != "") setMessage(message)

            setPositiveButton(posBtnTxtRes) { _, _ ->
                try {
                    val input = binding.et.string.toFloat()
                    listener.onDecimalDialogPositiveClick(input, tag)
                }
                catch (e: NumberFormatException) {
                    activity?.toast(getString(R.string.toast_err_no_input))
                }
            }
            setCancelButton(negBtnTxtRes)

            show()
        }
    }

    //

    interface DialogListener {
        fun onDecimalDialogPositiveClick(input: Float, tag: String?)
    }
}

fun decimalDialog(
    title: String,
    message: String = "",
    text: Float = NO_FLOAT_TEXT,
    hint: String = "",
    @StringRes posBtnTxtRes: Int = R.string.dialog_btn_ok,
    @StringRes negBtnTxtRes: Int = R.string.dialog_btn_cancel,
    tag: String
): DecimalDialog = DecimalDialog().apply {
    arguments = putBaseBundle(title, message, posBtnTxtRes, negBtnTxtRes, tag).apply {
        putFloat(ARG_TEXT, text)
        putString(ARG_HINT, hint)
    }
}