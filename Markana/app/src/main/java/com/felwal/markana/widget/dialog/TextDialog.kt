package com.felwal.markana.widget.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import com.felwal.markana.databinding.DialogTextBinding
import com.felwal.markana.util.string

private const val ARG_TEXT = "text"
private const val ARG_HINT = "hint"

private const val TAG_DEFAULT = "textDialog"

class TextDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
    private lateinit var text: String
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
            text = getString(ARG_TEXT, "")
            hint = getString(ARG_HINT, "")
        }
    }

    override fun buildDialog(): AlertDialog {
        val binding = DialogTextBinding.inflate(inflater)

        binding.et.setText(text)
        binding.et.hint = hint

        return builder.run {
            setView(binding.root)
            setTitle(title)
            if (!message.equals("")) setMessage(message)

            setPositiveButton(posBtnTxtRes) { _, _ ->
                val input = binding.et.string.trim { it <= ' ' }
                listener.onTextDialogPositiveClick(input, dialogTag)
            }
            setCancelButton(negBtnTxtRes)

            show()
        }
    }

    //

    interface DialogListener {
        fun onTextDialogPositiveClick(input: String, tag: String)
    }
}

fun textDialog(
    title: String, message: String = "",
    text: String = "", hint: String = "",
    @StringRes posBtnTxtRes: Int,
    tag: String
): TextDialog = TextDialog().apply {
    arguments = putBaseBundle(title, message, posBtnTxtRes, tag).apply {
        putString(ARG_TEXT, text)
        putString(ARG_HINT, hint)
    }
}