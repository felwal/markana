package com.felwal.markana.widget.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import com.felwal.markana.R
import com.felwal.markana.databinding.DialogRadioBinding
import com.felwal.markana.util.truncate

private const val ARG_RADIO_TEXTS = "radioTexts"
private const val ARG_SELECTED_INDEX = "selectedIndex"

private const val TAG_DEFAULT = "radioDialog"

class RadioDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // args
    private lateinit var radioTexts: List<String>
    private var selectedIndex = 0

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
            radioTexts = getStringArrayList(ARG_RADIO_TEXTS).orEmpty()
            selectedIndex = getInt(ARG_SELECTED_INDEX, 0).truncate(0, radioTexts.size)
        }
    }

    override fun buildDialog(): AlertDialog {
        val binding = DialogRadioBinding.inflate(inflater)

        return builder.run {
            setView(binding.root)
            setTitle(title)

            setCancelButton(negBtnTxtRes)

            // inflate radio buttons
            for (i in radioTexts.indices) {
                val rb = inflater.inflate(R.layout.item_dialog_radio, binding.rg, false) as RadioButton
                rb.text = radioTexts[i]
                binding.rg.addView(rb)

                rb.isChecked = i == selectedIndex

                // click
                rb.setOnClickListener {
                    dialog?.cancel()
                    listener.onRadioDialogClick(i, dialogTag)
                }
            }

            show()
        }
    }

    //

    interface DialogListener {
        fun onRadioDialogClick(index: Int, tag: String)
    }
}

fun radioDialog(
    title: String, message: String = "",
    radioButtonTexts: List<String>, selectedIndex: Int,
    tag: String
): RadioDialog = RadioDialog().apply {
    arguments = putBaseBundle(title, message, NO_RES, tag).apply {
        putStringArrayList(ARG_RADIO_TEXTS, ArrayList(radioButtonTexts))
        putInt(ARG_SELECTED_INDEX, selectedIndex)
    }
}