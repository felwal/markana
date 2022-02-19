package me.felwal.markana.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText
import me.felwal.android.util.hideKeyboard

class NotepadEditText(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    /**
     * Clear focus and hide keyboard on back press
     */
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean =
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            hideKeyboard()
            true
        }
        else false

    /**
     * Don't allow pasting text with rich text formatting
     */
    override fun onTextContextMenuItem(id: Int): Boolean =
        super.onTextContextMenuItem(if (id == android.R.id.paste) android.R.id.pasteAsPlainText else id)
}