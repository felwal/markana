package com.felwal.markana.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import com.felwal.markana.R
import com.felwal.markana.util.string

private const val BUNDLE_TEXT = "text"
private const val BUNDLE_HINT = "hint"

private const val TAG_DEFAULT = "textDialog"

class TextDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // arguments
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

    override fun unpackBundle() {
        val bundle: Bundle? = unpackBaseBundle(TAG_DEFAULT)

        bundle?.apply {
            text = getString(BUNDLE_TEXT, "")
            hint = getString(BUNDLE_HINT, "")
        }
    }

    override fun buildDialog(): AlertDialog {
        val vDialog: View = inflater.inflate(R.layout.dialog_text, null)
        val et = vDialog.findViewById<EditText>(R.id.et_dialog_text)

        et.setText(text)
        et.hint = hint

        if (!message.equals("")) builder.setMessage(message)

        builder
            .setView(vDialog)
            .setTitle(title)
            .setPositiveButton(posBtnTxtRes) { _, _ ->
                val input = et.string.trim { it <= ' ' }
                listener.onTextDialogPositiveClick(input, dialogTag)
            }
            .setCancelButton(negBtnTxtRes)

        return builder.show()
    }

    //

    interface DialogListener {
        fun onTextDialogPositiveClick(input: String, tag: String)
    }
}

fun textDialog(
    @StringRes titleRes: Int = NO_RES,
    @StringRes messageRes: Int = NO_RES,
    text: String = "",
    hint: String = "",
    @StringRes posBtnTxtRes: Int,
    tag: String
): TextDialog {
    val instance = TextDialog()
    val bundle: Bundle = putBaseBundle(titleRes, messageRes, posBtnTxtRes, tag)

    bundle.putString(BUNDLE_TEXT, text)
    bundle.putString(BUNDLE_HINT, hint)

    instance.arguments = bundle
    return instance
}