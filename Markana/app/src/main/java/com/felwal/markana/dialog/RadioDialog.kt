package com.felwal.markana.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.StringRes
import com.felwal.markana.R
import java.util.*

private const val BUNDLE_RADIO_TEXTS = "radioTexts"
private const val BUNDLE_SELECTED_INDEX = "selectedIndex"

private const val TAG_DEFAULT = "radioDialog"

class RadioDialog : BaseDialog() {

    private lateinit var listener: DialogListener

    // arguments
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

    override fun unpackBundle() {
        val bundle: Bundle? = unpackBaseBundle(TAG_DEFAULT)

        bundle?.apply {
            radioTexts = getStringArrayList(BUNDLE_RADIO_TEXTS) ?: arrayListOf()
            selectedIndex = getInt(BUNDLE_SELECTED_INDEX, 0)
        }

        // keep in range
        if (selectedIndex >= radioTexts.size) selectedIndex = radioTexts.size - 1
        else if (selectedIndex < 0) selectedIndex = 0
    }

    override fun buildDialog(): AlertDialog {
        val view: View = inflater.inflate(R.layout.dialog_radio, null)
        val group = view.findViewById<RadioGroup>(R.id.rg_dialog_radio)

        builder
            .setView(view)
            .setTitle(title)
            .setCancelButton(negBtnTxtRes)

        // inflate radio buttons
        for (i in radioTexts.indices) {
            val btn = inflater.inflate(R.layout.item_dialog_radio, group, false) as RadioButton
            btn.text = radioTexts[i]
            group.addView(btn)

            btn.isChecked = i == selectedIndex

            // click
            btn.setOnClickListener { _ ->
                dialog?.cancel()
                listener.onRadioDialogClick(i, dialogTag)
            }
        }
        return builder.show()
    }

    //

    interface DialogListener {
        fun onRadioDialogClick(index: Int, tag: String)
    }
}

fun radioDialog(
    @StringRes titleRes: Int = NO_RES,
    @StringRes messageRes: Int = NO_RES,
    radioButtonTexts: ArrayList<String>,
    selectedIndex: Int,
    tag: String
): RadioDialog {
    val instance = RadioDialog()
    val bundle: Bundle = putBaseBundle(titleRes, messageRes, NO_RES, tag)

    bundle.putStringArrayList(BUNDLE_RADIO_TEXTS, radioButtonTexts)
    bundle.putInt(BUNDLE_SELECTED_INDEX, selectedIndex)

    instance.arguments = bundle
    return instance
}