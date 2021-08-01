package com.felwal.markana.dialog

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
const val NO_FLOAT_TEXT = -1f

const val BUNDLE_TITLE_RES = "title"
const val BUNDLE_MESSAGE_RES = "message"
const val BUNDLE_POSITIVE_BUTTON_RES = "positiveButtonText"
const val BUNDLE_NEGATIVE_BUTTON_RES = "negativeButtonText"
const val BUNDLE_TAG = "tag"

abstract class BaseDialog : DialogFragment() {

    protected lateinit var builder: AlertDialog.Builder
    protected lateinit var inflater: LayoutInflater

    // arguments
    protected var title: String? = null
    protected var message: String? = null
    protected var dialogTag: String = "baseDialog"

    @StringRes protected var posBtnTxtRes: Int = R.string.dialog_btn_ok
    @StringRes protected var negBtnTxtRes: Int = R.string.dialog_btn_cancel

    // DialogFragment

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(activity)
        inflater = requireActivity().layoutInflater

        unpackBundle()

        return styleDialog(buildDialog())
    }

    // bundle

    protected abstract fun unpackBundle()

    protected fun unpackBaseBundle(defaultTag: String): Bundle? {
        val bundle: Bundle? = arguments

        bundle?.apply {
            val titleRes = getInt(BUNDLE_TITLE_RES, NO_RES)
            val messageRes = getInt(BUNDLE_MESSAGE_RES, NO_RES)
            posBtnTxtRes = getInt(BUNDLE_POSITIVE_BUTTON_RES, posBtnTxtRes)
            negBtnTxtRes = getInt(BUNDLE_NEGATIVE_BUTTON_RES, negBtnTxtRes)
            dialogTag = getString(BUNDLE_TAG, defaultTag)

            // convert from res
            title = if (titleRes == NO_RES) "" else getString(titleRes)
            message = if (messageRes == NO_RES) "" else getString(messageRes)
        }

        return bundle
    }

    // build

    private fun styleDialog(dialog: AlertDialog): AlertDialog {
        // title
        dialog.setTitleTextAppearance(resources, R.style.TextView_DialogTitle)

        // message
        dialog.setMessageTextAppearance(
            if (title == "") R.style.TextView_DialogMessageLone
            else R.style.TextView_DialogMessage
        )

        // bg
        dialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog_bg)

        return dialog
    }

    protected abstract fun buildDialog(): AlertDialog

    fun show(fm: FragmentManager) {
        if (!isAdded) super.show(fm, dialogTag)
    }
}

fun putBaseBundle(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    @StringRes posBtnTxtRes: Int,
    tag: String
): Bundle {
    val bundle = Bundle()

    bundle.putInt(BUNDLE_TITLE_RES, titleRes)
    bundle.putInt(BUNDLE_MESSAGE_RES, messageRes)
    bundle.putInt(BUNDLE_POSITIVE_BUTTON_RES, posBtnTxtRes)
    bundle.putString(BUNDLE_TAG, tag)

    return bundle
}

fun AlertDialog.Builder.setCancelButton(@StringRes resId: Int): AlertDialog.Builder =
    setNegativeButton(resId) { dialog, _ ->
        dialog.cancel()
    }

fun AlertDialog.setTitleTextAppearance(res: Resources, @StyleRes resId: Int) {
    val titleId: Int = res.getIdentifier("alertTitle", "id", "android")

    if (titleId > 0) {
        val tvTitle = findViewById<TextView>(titleId)
        tvTitle?.setTextAppearance(resId)
    }
}

fun AlertDialog.setMessageTextAppearance(@StyleRes resId: Int) =
    findViewById<TextView>(android.R.id.message)?.setTextAppearance(resId)