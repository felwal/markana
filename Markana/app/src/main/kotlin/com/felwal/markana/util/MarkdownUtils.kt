package com.felwal.markana.util

import android.text.Editable
import android.widget.EditText
import com.felwal.markana.prefs

private val emph get() = prefs.emphSymbol
private val strong get() = prefs.strongSymbol
private const val strikethrough = "~~"
private const val code = "`"
private fun header(level: Int) = "${"#".repeat(level)} "
private const val quote = "> "
private val bullet get() = "${prefs.bulletlistSymbol} "
private fun number(i: Int) = "${i + 1}. "
private val checkbox get() = "$bullet[${if (prefs.checkboxSpace) " " else ""}] "
private val thematicBreak get() = "${prefs.breakSymbol}\n"

// specifics

fun EditText.toggleEmph() = formatSelectedText(emph)

fun EditText.toggleStrong() = formatSelectedText(strong)

fun EditText.toggleStrikethrough() = formatSelectedText(strikethrough)

fun EditText.toggleCode() = formatSelectedText(code)

fun EditText.toggleHeader() = formatSelectedLines { header(1) }

fun EditText.toggleQuote() = formatSelectedLines { quote }

fun EditText.toggleBulletlist() = formatSelectedLines { bullet }

fun EditText.toggleNumberlist() = formatSelectedLines { number(it) }

fun EditText.toggleChecklist() = formatSelectedLines { checkbox }

fun EditText.insertThematicBreak() = insertAtCursor(thematicBreak)

// generals

private fun EditText.insertAtCursor(marker: String) {
    val textCopy = text.copy
    val end = selectionEnd

    textCopy.insert(end, marker)

    text = textCopy
    setSelection(end + marker.length)
}

private fun EditText.formatSelectedText(marker: String) {
    val textCopy = text.copy
    val start = selectionStart
    val end = selectionEnd

    // TODO: mark multi-paragraph, ie **hi**\n\n**by** instead of **hi\n\nby**

    // toggle: remove marker outside selection, ie **|hi|**
    if (removeTextMarker(textCopy, marker,start - marker.length, start, end, end + marker.length)) {
        text = textCopy
        setSelection(start - marker.length, end - marker.length)
        return
    }
    // toggle: remove marker inside selection, ie |**hi**|
    if (removeTextMarker(textCopy, marker, start, start + marker.length, end - marker.length, end)) {
        text = textCopy
        setSelection(start, end - 2 * marker.length)
        return
    }
    // TODO: also toggle |**hi|**, *|*hi|** and *|*hi*|*
    // TODO: dont recognize and toggle **hi** as *hi*

    // insert at end first to not alter index of start
    textCopy.insert(end, marker)
    textCopy.insert(start, marker)

    text = textCopy
    setSelection(start + marker.length, end + marker.length)
}

private fun removeTextMarker(
    editable: Editable, marker: String,
    openingMarkerStart: Int, openingMarkerEnd: Int,
    closingMarkerStart: Int, closingMarkerEnd: Int
): Boolean {
    if (
        editable.substring(openingMarkerStart, openingMarkerEnd) == marker
        && editable.substring(closingMarkerStart, closingMarkerEnd) == marker
    ) {
        // delete closing first to not alter index of opening
        editable.delete(closingMarkerStart, closingMarkerEnd)
        editable.delete(openingMarkerStart, openingMarkerEnd)
        return true
    }
    return false
}

private fun EditText.formatSelectedLines(marker: (lineIndex: Int) -> String) {
    val textCopy = text.copy
    val start = selectionStart
    val end = selectionEnd

    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end)

    var startOffset = 0
    var endOffset = 0
    // go backwards to not alter index of earlier lines
    for (line in endLine downTo startLine) {
        val lineStart = layout.getLineStart(line)
        val lineIndex = line - startLine

        // TODO: check for empty lines, then add marker only there

        // TODO: toggle between different list types

        // toggle: remove marker
        if (removeLineMarker(textCopy, marker(lineIndex), lineStart)) {
            endOffset -= marker(lineIndex).length
            if (line == startLine) startOffset -= marker(startLine).length
        }
        // toggle: add marker
        else {
            textCopy.insert(lineStart, marker(lineIndex))
            endOffset += marker(lineIndex).length
            if (line == startLine) startOffset += marker(startLine).length
        }
    }

    text = textCopy
    setSelection(start + startOffset, end + endOffset)
}

private fun removeLineMarker(editable: Editable, marker: String, lineStart: Int): Boolean {
    if (editable.substring(lineStart, lineStart + marker.length) == marker) {
        editable.delete(lineStart, lineStart + marker.length)
        return true
    }
    return false
}