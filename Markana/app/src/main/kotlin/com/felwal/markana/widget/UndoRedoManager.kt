package com.felwal.markana.widget

import android.widget.EditText
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import com.felwal.markana.util.updateEditable
import com.felwal.markana.util.removeAll
import kotlin.collections.ArrayDeque

class UndoRedoManager(private val et: EditText) {

    private val historyBuffer = ArrayDeque<Command>()
    private val futureBuffer = ArrayDeque<Command>()

    val canUndo: Boolean get() = historyBuffer.isNotEmpty()
    val canRedo: Boolean get() = futureBuffer.isNotEmpty()
    private var textUndoedOrRedoed = false

    init {
        var command: Command? = null

        et.doBeforeTextChanged { text, start, count, _ ->
            text?.let {
                val oldText = it.substring(start, start + count)
                command = registerOldText(start, oldText)
            }
        }
        et.doOnTextChanged { text, start, _, count ->
            // dont update when text is entered via undo/redo (and tempCommand therefore null)
            command?.let { command ->
                text?.let {
                    val newText = it.substring(start, start + count)
                    registerNewText(command, newText)
                }
            }
        }
    }

    private fun registerOldText(oldStart: Int, oldText: String): Command? {
        // dont update when text is entered via undo/redo
        return if (textUndoedOrRedoed) {
            textUndoedOrRedoed = false
            null
        }
        else Command(oldStart, oldText)
    }

    private fun registerNewText(command: Command, newText: String) {
        command.newText = newText
        if (canRedo) futureBuffer.removeAll()
        historyBuffer.add(command)
    }

    fun undo() {
        if (!canUndo) return

        val lastCommand = historyBuffer.removeLast()
        futureBuffer.addLast(lastCommand)
        textUndoedOrRedoed = true

        et.updateEditable {
            replace(lastCommand.start, lastCommand.newEnd, lastCommand.oldText)
        }
        et.setSelection(lastCommand.oldEnd)
    }

    fun redo() {
        if (!canRedo) return

        val nextCommand = futureBuffer.removeLast()
        historyBuffer.addLast(nextCommand)
        textUndoedOrRedoed = true

        et.updateEditable {
            replace(nextCommand.start, nextCommand.oldEnd, nextCommand.newText)
        }
        et.setSelection(nextCommand.newEnd)
    }

    private class Command(val start: Int, val oldText: String) {
        lateinit var newText: String
        val oldEnd get() = start + oldText.length
        val newEnd get() = start + newText.length
    }
}