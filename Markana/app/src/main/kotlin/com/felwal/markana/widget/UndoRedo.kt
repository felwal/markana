package com.felwal.markana.widget

import android.widget.EditText
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import com.felwal.markana.util.copy
import com.felwal.markana.util.removeAllFrom

class StringHistory {

    private val history = mutableListOf<StringHistoryLayer>()
    private var cursor =  -1
    private var tempLayer: StringHistoryLayer? = null

    val canUndo: Boolean get() = cursor > -1
    val canRedo: Boolean get() = cursor < history.size - 1
    private var textUndoedOrRedoed = false

    fun registerOldText(oldStart: Int, oldText: String) {
        // dont execute this when text is entered via undo/redo
        if (textUndoedOrRedoed) return

        tempLayer = StringHistoryLayer(oldStart, oldText)
    }

    fun registerNewText(newText: String) {
        // dont execute this when text is entered via undo/redo
        if (textUndoedOrRedoed) {
            textUndoedOrRedoed = false
            return
        }

        tempLayer?.let {
            it.newText = newText
            if (canRedo) history.removeAllFrom(cursor + 1)
            history.add(it)
            cursor += 1
        }
        tempLayer = null
    }

    fun undo(et: EditText) {
        if (!canUndo) return

        val textCopy = et.text.copy
        val currentLayer = history[cursor]
        textUndoedOrRedoed = true

        textCopy.replace(currentLayer.start, currentLayer.newEnd, currentLayer.oldText)
        cursor -= 1

        et.text = textCopy
        et.setSelection(currentLayer.oldEnd)
    }

    fun redo(et: EditText) {
        if (!canRedo) return

        val textCopy = et.text.copy
        val currentLayer = history[cursor]
        textUndoedOrRedoed = true

        cursor += 1
        textCopy.replace(currentLayer.start, currentLayer.oldEnd, currentLayer.newText)

        et.text = textCopy
        et.setSelection(currentLayer.newEnd)
    }

    private class StringHistoryLayer(val start: Int, val oldText: String) {
        lateinit var newText: String
        val oldEnd get() = start + oldText.length
        val newEnd get() = start + newText.length
    }
}

fun EditText.registerUndoRedo(history: StringHistory) {
    doBeforeTextChanged { text, start, count, _ ->
        text?.let {
            val oldText = it.substring(start, start + count)
            history.registerOldText(start, oldText)
        }
    }
    doOnTextChanged { text, start, _, count ->
        text?.let {
            val newText = it.substring(start, start + count)
            history.registerNewText(newText)
        }
    }
}

fun EditText.undo(history: StringHistory) {
    history.undo(this)
}

fun EditText.redo(history: StringHistory) {
    history.redo(this)
}