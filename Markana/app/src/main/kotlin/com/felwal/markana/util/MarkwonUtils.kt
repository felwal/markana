package com.felwal.markana.util

import android.text.Editable
import android.text.Spanned
import android.text.style.MetricAffectingSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorUtils
import io.noties.markwon.editor.PersistedSpans

inline fun <reified S> MarkwonEditor.Builder.useEditHandler(
    instance: S,
    delimeter1: String,
    delimeter2: String? = null
): MarkwonEditor.Builder {
    useEditHandler(object : AbstractEditHandler<S>() {
        override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
            builder.persistSpan(S::class.java) { instance }
        }

        override fun handleMarkdownSpan(
            persistedSpans: PersistedSpans,
            editable: Editable,
            input: String,
            span: S,
            spanStart: Int,
            spanTextLength: Int
        ) {
            if (delimeter2 == null) {
                MarkwonEditorUtils.findDelimited(input, spanStart, delimeter1)
            }
            else {
                MarkwonEditorUtils.findDelimited(input, spanStart, delimeter1, delimeter2)
            }?.let {
                editable.setSpan(
                    persistedSpans.get(S::class.java),
                    it.start(), it.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        override fun markdownSpanType(): Class<S> = S::class.java
    })

    return this
}