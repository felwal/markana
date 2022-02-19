package com.felwal.markana.util

import android.text.Editable
import android.text.Layout
import android.widget.EditText
import com.felwal.markana.prefs
import me.felwal.android.util.coerceSelection
import me.felwal.android.util.coerceSubstring
import me.felwal.android.util.copy
import me.felwal.android.util.updateEditable

private val emph get() = prefs.emphSymbol
private val strong get() = prefs.strongSymbol
private const val strikethrough = "~~"
private const val code = "`"
private fun header(level: Int) = "#".repeat(level) + " "
private const val quote = "> "
private val bullet get() = prefs.bulletListSymbol + " "
private fun number(i: Int) = "${i + 1}. "
private fun checkbox(checked: Boolean) = "$bullet[${if (checked) "x" else " "}] "
private const val indent = "  "
private val thematicBreak get() = prefs.breakSymbol + "\n"

// specific

fun EditText.toggleEmph() = formatSelectedText(emph)

fun EditText.toggleStrong() = formatSelectedText(strong)

fun EditText.toggleStrikethrough() = formatSelectedText(strikethrough)

fun EditText.toggleCode() = formatSelectedText(code)

fun EditText.toggleHeader() = formatSelectedParagraphs(
    header(1),
    header(1) to header(2),
    header(2) to header(3),
    header(3) to header(4),
    header(4) to header(5),
    header(5) to header(6),
    header(6) to "",
)

fun EditText.toggleQuote() = formatSelectedParagraphs { quote }

fun EditText.toggleBulletList() = formatSelectedParagraphs { bullet }

fun EditText.toggleNumberList() = formatSelectedParagraphs { number(it) }

fun EditText.toggleChecklist() = formatSelectedParagraphs(
    checkbox(false),
    checkbox(false) to checkbox(true),
    checkbox(true) to ""
)

fun EditText.outdent() = formatSelectedParagraphs("", indent to "")

fun EditText.indent() = formatSelectedParagraphs(indent)

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

private fun EditText.formatSelectedParagraphs(defaultMarker: String, vararg markerRules: Pair<String, String>) {
    val textCopy = text.copy()
    val start = selectionStart
    val end = selectionEnd

    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end)
    val paragraphStarts = (endLine downTo startLine)
        .map { layout.getParagraphStart(layout.getLineStart(it)) }
        .toSet()

    var startOffset = 0
    var endOffset = 0

    // go backwards to not alter index of earlier paragraph
    for ((reversedIndex, paragraphStart) in paragraphStarts.withIndex()) {
        val paragraphIndex = paragraphStarts.size - 1 - reversedIndex

        // cycle through rules until a matching one is found and executed
        var ruleApplied = false
        for (rule in markerRules) {
            if (isParagraphMarked(textCopy, rule.first, paragraphStart)) {
                textCopy.delete(paragraphStart, paragraphStart + rule.first.length)
                textCopy.insert(paragraphStart, rule.second)

                // offset selection
                endOffset += rule.second.length - rule.first.length
                if (paragraphIndex == 0) startOffset += rule.second.length - rule.first.length

                ruleApplied = true
                break
            }
        }
        // ... or execute default rule
        if (!ruleApplied) {
            textCopy.insert(paragraphStart, defaultMarker)

            // offset selection
            endOffset += defaultMarker.length
            if (paragraphIndex == 0) startOffset += defaultMarker.length
        }
    }

    // apply
    text = textCopy
    val newStart = (start + startOffset).coerceAtLeast(paragraphStarts.last())
    val newEnd = (end + endOffset).coerceAtLeast(paragraphStarts.last())
    coerceSelection(newStart, newEnd)
}

// TODO: rules
private fun EditText.formatSelectedParagraphs(marker: (paragraphIndex: Int) -> String) {
    val textCopy = text.copy()
    val start = selectionStart
    val end = selectionEnd

    val startLine = layout.getLineForOffset(start)
    val endLine = layout.getLineForOffset(end)
    val paragraphStarts = (endLine downTo startLine)
        .map { layout.getParagraphStart(layout.getLineStart(it)) }
        .toSet()

    var startOffset = 0
    var endOffset = 0

    // storing actions and executing later is neccessary to determine if to toggle on or off
    val removeMarkerActions = mutableListOf<() -> Unit>()
    val addMarkerActions = mutableListOf<() -> Unit>()

    // go backwards to not alter index of earlier paragraph
    for ((reversedIndex, paragraphStart) in paragraphStarts.withIndex()) {
        val paragraphIndex = paragraphStarts.size - 1 - reversedIndex

        // TODO: toggle between different list types

        // toggle: remove marker
        if (isParagraphMarked(textCopy, marker(paragraphIndex), paragraphStart)) {
            removeMarkerActions.add {
                textCopy.delete(paragraphStart, paragraphStart + marker(paragraphIndex).length)

                // offset selection
                endOffset -= marker(paragraphIndex).length
                if (paragraphIndex == 0) startOffset -= marker(paragraphIndex).length
            }
        }
        // toggle: add marker
        else {
            addMarkerActions.add {
                textCopy.insert(paragraphStart, marker(paragraphIndex))

                // offset selection
                endOffset += marker(paragraphIndex).length
                if (paragraphIndex == 0) startOffset += marker(paragraphIndex).length
            }
        }
    }

    // prioritize toggling on unmarked paragraphs before toggling off marked paragraph
    if (addMarkerActions.size > 0) addMarkerActions.forEach { it() }
    else removeMarkerActions.forEach { it() }

    // apply
    text = textCopy
    val newStart = (start + startOffset).coerceAtLeast(paragraphStarts.last())
    val newEnd = (end + endOffset).coerceAtLeast(paragraphStarts.last())
    coerceSelection(newStart, newEnd)
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

private fun isParagraphMarked(editable: Editable, marker: String, paragraphStart: Int): Boolean =
    editable.coerceSubstring(paragraphStart, paragraphStart + marker.length) == marker

private fun Layout.getParagraphStart(index: Int): Int {
    for (i in index downTo 0) {
        if (text.coerceSubstring(i - 1, i) == "\n") return i
    }
    // the paragraph is the first paragraph; return layout start
    return 0
}