package com.felwal.markana.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.felwal.markana.util.hideKeyboard

/**
 * An EditText which clears focus and hides keyboard on back press.
 */
class TimidEditText(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            hideKeyboard()
            return true
        }
        return false
    }
}