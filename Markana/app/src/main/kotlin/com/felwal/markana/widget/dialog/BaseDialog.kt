package com.felwal.markana.widget.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.felwal.markana.R

const val NO_RES = -1

private const val ARG_TITLE = "title"
private const val ARG_MESSAGE = "message"
private const val ARG_POSITIVE_BUTTON_RES = "positiveButtonText"
private const val ARG_NEGATIVE_BUTTON_RES = "negativeButtonText"
private const val ARG_TAG = "tag"

abstract class BaseDialog : DialogFragment() {

    protected lateinit var builder: AlertDialog.Builder
    protected lateinit var inflater: LayoutInflater

    // args
    protected lateinit var title: String
    protected lateinit var message: String
    protected var dialogTag: String = "baseDialog"

    @StringRes protected var posBtnTxtRes: Int = R.string.dialog_btn_ok
    @StringRes protected var negBtnTxtRes: Int = R.string.dialog_btn_cancel

    // DialogFragment

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(activity)
        inflater = requireActivity().layoutInflater

        unpackBundle(unpackBaseBundle())

        return styleDialog(buildDialog())
    }

    // bundle

    fun putBaseBundle(
        title: String,
        message: String,
        @StringRes posBtnTxtRes: Int = this.posBtnTxtRes,
        @StringRes negBtnTxtRes: Int = this.negBtnTxtRes,
        tag: String
    ): Bundle = Bundle().apply {
        putString(ARG_TITLE, title)
        putString(ARG_MESSAGE, message)
        putInt(ARG_POSITIVE_BUTTON_RES, posBtnTxtRes)
        putInt(ARG_NEGATIVE_BUTTON_RES, negBtnTxtRes)
        putString(ARG_TAG, tag)
    }

    protected abstract fun unpackBundle(bundle: Bundle?)

    private fun unpackBaseBundle(): Bundle? = arguments?.apply {
        title = getString(ARG_TITLE, "")
        message = getString(ARG_MESSAGE, "")
        posBtnTxtRes = getInt(ARG_POSITIVE_BUTTON_RES, posBtnTxtRes)
        negBtnTxtRes = getInt(ARG_NEGATIVE_BUTTON_RES, negBtnTxtRes)
        dialogTag = getString(ARG_TAG, "dialog")
    }

    // build

    private fun styleDialog(dialog: AlertDialog): AlertDialog = dialog.apply {
        setTitleTextAppearance(resources, R.style.TextView_DialogTitle)
        setMessageTextAppearance(
            if (title == "") R.style.TextView_DialogMessageLone
            else R.style.TextView_DialogMessage
        )
        window?.setBackgroundDrawableResource(R.drawable.shape_dialog_bg)
    }

    protected abstract fun buildDialog(): AlertDialog

    fun show(fm: FragmentManager) {
        if (!isAdded) super.show(fm, dialogTag)
    }
}

fun AlertDialog.Builder.setCancelButton(@StringRes resId: Int): AlertDialog.Builder =
    setNegativeButton(resId) { dialog, _ ->
        dialog.cancel()
    }

fun AlertDialog.setTitleTextAppearance(res: Resources, @StyleRes resId: Int) =
    res.getIdentifier("alertTitle", "id", "android")
        .takeIf { it > 0 }
        ?.let { titleId ->
            findViewById<TextView>(titleId)?.setTextAppearance(resId)
        }

fun AlertDialog.setMessageTextAppearance(@StyleRes resId: Int) =
    findViewById<TextView>(android.R.id.message)?.setTextAppearance(resId)