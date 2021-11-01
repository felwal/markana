package com.felwal.android.widget.dialog

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.felwal.android.R
import com.felwal.android.databinding.DialogTextBinding
import com.felwal.android.util.string
import com.felwal.android.util.toast

const val NO_INT_TEXT = -1

private const val ARG_TEXT = "text"
private const val ARG_HINT = "hint"

class NumberDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
    private var text = 0
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
            text = getInt(ARG_TEXT, 0)
            hint = getString(ARG_HINT, "")
        }
    }

    override fun buildDialog(): AlertDialog {
        val binding = DialogTextBinding.inflate(inflater)
        binding.et.inputType = EditorInfo.TYPE_CLASS_NUMBER

        binding.et.hint = hint
        if (text != NO_INT_TEXT) binding.et.setText(text.toString())

        return builder.run {
            setView(binding.root)
            setTitle(title)
            if (message != "") setMessage(message)

            setPositiveButton(posBtnTxtRes) { _, _ ->
                try {
                    val input = binding.et.string.toInt()
                    listener.onNumberDialogPositiveClick(input, dialogTag)
                }
                catch (e: NumberFormatException) {
                    activity?.toast(R.string.toast_e_input)
                }
            }
            setCancelButton(negBtnTxtRes)

            show()
        }
    }

    //

    interface DialogListener {
        fun onNumberDialogPositiveClick(input: Int, tag: String?)
    }

    //

    companion object {
        @JvmStatic
        fun newInstance(
            title: String,
            message: String = "",
            text: Int = NO_INT_TEXT,
            hint: String = "",
            @StringRes posBtnTxtRes: Int = R.string.dialog_btn_ok,
            @StringRes negBtnTxtRes: Int = R.string.dialog_btn_cancel,
            tag: String
        ): NumberDialog = NumberDialog().apply {
            arguments = putBaseBundle(title, message, posBtnTxtRes, negBtnTxtRes, tag).apply {
                putInt(ARG_TEXT, text)
                putString(ARG_HINT, hint)
            }
        }
    }
}

fun numberDialog(
    title: String,
    message: String = "",
    text: Int = NO_INT_TEXT,
    hint: String = "",
    @StringRes posBtnTxtRes: Int = R.string.dialog_btn_ok,
    @StringRes negBtnTxtRes: Int = R.string.dialog_btn_cancel,
    tag: String
): NumberDialog = NumberDialog.newInstance(title, message, text, hint, posBtnTxtRes, negBtnTxtRes, tag)