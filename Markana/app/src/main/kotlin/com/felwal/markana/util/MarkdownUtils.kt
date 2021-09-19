package com.felwal.markana.util

import android.text.Editable
import android.text.Layout
import android.widget.EditText
import com.felwal.markana.prefs

private val emph get() = prefs.emphSymbol
private val strong get() = prefs.strongSymbol
private const val strikethrough = "~~"
private const val code = "`"
private fun header(level: Int) = "#".repeat(level) + " "
private const val quote = "> "
private val bullet get() = prefs.bulletListSymbol + " "
private fun number(i: Int) = "${i + 1}. "
private fun checkbox(checked: Boolean) = "$bullet[${if (checked) "x" else if (prefs.checkboxSpace) " " else ""}] "
private const val indent = "  "
private val thematicBreak get() = prefs.breakSymbol + "\n"

// specific

fun EditText.toggleEmph() = formatSelectedText(emph)

fun EditText.toggleStrong() = formatSelectedText(strong)

fun EditText.toggleStrikethrough() = formatSelectedText(strikethrough)

fun EditText.toggleCode() = formatSelectedText(code)

fun EditText.toggleHeader() = formatSelectedLines(
    header(1),
    header(1) to header(2),
    header(2) to header(3),
    header(3) to header(4),
    header(4) to header(5),
    header(5) to header(6),
    header(6) to "",
)

fun EditText.toggleQuote() = formatSelectedLines { quote }

fun EditText.toggleBulletList() = formatSelectedLines { bullet }

fun EditText.toggleNumberList() = formatSelectedLines { number(it) }

fun EditText.toggleChecklist() = formatSelectedLines(
    checkbox(false),
    checkbox(false) to checkbox(true),
    checkbox(true) to ""
)

fun EditText.outdent() = formatSelectedLines("", indent to "")

fun EditText.indent() = formatSelectedLines(indent)

fun EditText.insertThematicBreak() = insertAtCursor(thematicBreak)

// general

private fun EditText.insertAtCursor(marker: String) {
    val end = selectionEnd

    updateEditable {
        insert(end, marker)
    }

    coerceSelection(end + marker.length)
}

private fun EditText.formatSelectedText(marker: String) {
    val textCopy = text.copy()
    val start = selectionStart
    val end = selectionEnd

    // TODO: add rules

    // TODO: mark multi-paragraph, ie **hi**\n\n**by** instead of **hi\n\nby**
    /*val selection = textCopy.substring(start, end)
    if (selection.contains("\n\n")) {}*/

    // toggle: remove marker outside selection, ie **|hi|**
    if (removeTextMarker(textCopy, marker, start - marker.length, start, end, end + marker.length)) {
        text = textCopy
        coerceSelection(start - marker.length, end - marker.length)
        return
    }
    // toggle: remove marker inside selection, ie |**hi**|
    if (removeTextMarker(textCopy, marker, start, start + marker.length, end - marker.length, end)) {
        text = textCopy
        coerceSelection(start, end - 2 * marker.length)
        return
    }
    // TODO: also toggle |**hi|**, *|*hi|** and *|*hi*|*
    // TODO: dont recognize and toggle **hi** as *hi*

    // toggle: add marker
    // insert at end first to not alter index of start
    textCopy.insert(end, marker)
    textCopy.insert(start, marker)

    // apply
    text = textCopy
    coerceSelection(start + marker.length, end + marker.length)
}

// TODO: rules
private fun EditText.formatSelectedLines(defaultMarker: String, vararg markerRules: Pair<String, String>) {
    val textCopy = text.copy()
    val start = selectionStart
    val end = selectionEnd

    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end)

    var startOffset = 0
    var endOffset = 0

    for (line in endLine downTo startLine) {
        val lineStart = layout.getLineStart(line)
        val lineIndex = line - startLine

        var ruleApplied = false
        for (rule in markerRules) {
            if (isLineMarked(textCopy, rule.first, lineStart)) {
                textCopy.delete(lineStart, lineStart + rule.first.length)
                textCopy.insert(lineStart, rule.second)

                endOffset += rule.second.length - rule.first.length
                if (line == startLine) startOffset += rule.second.length - rule.first.length

                ruleApplied = true
                break
            }
        }
        if (!ruleApplied) {
            textCopy.insert(lineStart, defaultMarker)

            endOffset += defaultMarker.length
            if (line == startLine) startOffset += defaultMarker.length
        }
    }

    // apply
    text = textCopy
    coerceSelection(start + startOffset, end + endOffset)
}

private fun EditText.formatSelectedLines(marker: (lineIndex: Int) -> String) {
    val textCopy = text.copy()
    val start = selectionStart
    val end = selectionEnd

    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end)

    val removeMarkerActions = mutableListOf<() -> Unit>()
    val addMarkerActions = mutableListOf<() -> Unit>()

    var startOffset = 0
    var endOffset = 0

    // go backwards to not alter index of earlier lines
    for (line in endLine downTo startLine) {
        val lineStart = layout.getLineStart(line)
        val lineIndex = line - startLine

        // TODO: toggle between different list types

        // toggle: remove marker
        if (isLineMarked(textCopy, marker(lineIndex), lineStart)) {
            removeMarkerActions.add {
                textCopy.delete(lineStart, lineStart + marker(lineIndex).length)

                endOffset -= marker(lineIndex).length
                if (line == startLine) startOffset -= marker(startLine).length
            }
        }
        // toggle: add marker
        else {
            addMarkerActions.add {
                textCopy.insert(lineStart, marker(lineIndex))

                endOffset += marker(lineIndex).length
                if (line == startLine) startOffset += marker(startLine).length
            }
        }
    }

    // prioritize toggling on unmarked lines before toggling off marked lines
    if (addMarkerActions.size > 0) addMarkerActions.forEach { it() }
    else removeMarkerActions.forEach { it() }

    // apply
    text = textCopy
    coerceSelection(start + startOffset, end + endOffset)
}

// tool

private fun removeTextMarker(
    editable: Editable, marker: String,
    openingMarkerStart: Int, openingMarkerEnd: Int,
    closingMarkerStart: Int, closingMarkerEnd: Int
): Boolean {
    if (
        editable.coerceSubstring(openingMarkerStart, openingMarkerEnd) == marker
        && editable.coerceSubstring(closingMarkerStart, closingMarkerEnd) == marker
    ) {
        // delete closing first to not alter index of opening
        editable.delete(closingMarkerStart, closingMarkerEnd)
        editable.delete(openingMarkerStart, openingMarkerEnd)
        return true
    }
    return false
}

private fun isLineMarked(editable: Editable, marker: String, lineStart: Int): Boolean =
    editable.coerceSubstring(lineStart, lineStart + marker.length) == marker

private fun Layout.getParagraphStart(index: Int): Int {
    for (i in index downTo 0) {
        if (text.substring(i, i + 1) == "\n") return i
    }
    return -1
}

private fun EditText.coerceSelection(start: Int, stop: Int? = null) =
    if (stop == null) setSelection(start.coerceIn(0, length()))
    else setSelection(start.coerceIn(0, length()), stop.coerceIn(0, length()))