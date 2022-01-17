package com.felwal.markana.ui.labelpager

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felwal.android.util.getColorByAttr
import com.felwal.android.util.multiplyAlphaComponent
import com.felwal.android.util.px
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.data.prefs.SortBy
import com.felwal.markana.databinding.ItemNotelistGridNoteBinding
import com.felwal.markana.prefs
import com.felwal.markana.util.FORMATTER_EARLIER
import com.felwal.markana.util.FORMATTER_THIS_YEAR
import com.felwal.markana.util.FORMATTER_TODAY
import com.felwal.markana.util.atStartOfYear
import com.felwal.markana.util.fromEpochSecond
import java.time.LocalDate
import java.time.LocalDateTime

class NoteListListAdapter(
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : ListAdapter<Note, RecyclerView.ViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemNotelistGridNoteBinding.inflate(inflater)
        return GridNoteViewHolder(parent.context, binding, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = getItem(position)
        if (holder is GridNoteViewHolder) holder.bind(note)
    }

    // viewholder

    class GridNoteViewHolder(
        private val c: Context,
        private val binding: ItemNotelistGridNoteBinding,
        private val onClick: (Note) -> Unit,
        private val onLongClick: (Note) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentNote: Note? = null

        init {
            binding.clNote.setOnClickListener {
                currentNote?.let { onClick(it) }
            }
            binding.clNote.setOnLongClickListener {
                currentNote?.let { onLongClick(it) }
                return@setOnLongClickListener true
            }
        }

        fun bind(note: Note) {
            currentNote = note

            // title
            binding.tvTitle.text = if (prefs.notePreviewMime) note.filename else note.filenameWithoutExtension
            binding.tvTitle.setTextColor(note.getColor(c))

            // body
            when {
                prefs.notePreviewMaxLines <= 0 -> {
                    binding.tvBody.isGone = true
                }
                note.content == "" -> {
                    binding.tvBody.maxLines = prefs.notePreviewMaxLines
                    binding.tvBody.isGone = true
                }
                else -> {
                    binding.tvBody.maxLines = prefs.notePreviewMaxLines
                    binding.tvBody.isGone = false
                    binding.tvBody.text = note.content.trim() // TODO: dont get the full string
                }
            }
            binding.tvBody.setTextColor(
                if (!note.isArchived) c.getColorByAttr(android.R.attr.textColorSecondary)
                else c.getColorByAttr(android.R.attr.textColorSecondary).multiplyAlphaComponent(0.35f)
            )

            // metadata
            binding.tvMetadata.isGone = !prefs.notePreviewMetadata
            if (prefs.notePreviewMetadata) {
                binding.tvMetadata.text =
                    if (prefs.sortBy == SortBy.OPENED) {
                        "Opened ${note.opened?.fromEpochSecond()?.formatNoteItem() ?: "never"}"
                    }
                    else {
                        "Modified ${note.modified?.fromEpochSecond()?.formatNoteItem() ?: "never"}"
                    }
            }
            binding.tvMetadata.setTextColor(
                if (!note.isArchived) c.getColorByAttr(android.R.attr.textColorTertiary)
                else c.getColorByAttr(android.R.attr.textColorTertiary).multiplyAlphaComponent(0.35f)
            )

            // container: fill
            binding.clNote.background.setTintMode(PorterDuff.Mode.SRC_OVER)
            if (prefs.notePreviewColor) {
                binding.ivBorder.isGone = true
                binding.clNote.background.setTint(note.getBackgroundColor(c))
            }
            // container: stroke
            else {
                binding.ivBorder.isGone = false
                binding.clNote.background.setTint(c.getColorByAttr(android.R.attr.colorBackground))

                // check to not set stroke twice
                if (!note.isSelected) {
                    (binding.ivBorder.background as GradientDrawable)
                        .setStroke(1.px, c.getColorByAttr(android.R.attr.listDivider))
                }
            }

            // mark selected (with stroke)
            if (note.isSelected) {
                binding.ivBorder.isGone = false
                (binding.ivBorder.background as GradientDrawable)
                    .setStroke(1.px, c.getColorByAttr(R.attr.colorOnBackground))
            }

            // item icon
            binding.ivItem.drawable.setTint(note.getColor(c))
            binding.ivItem.isGone = prefs.gridView || !prefs.notePreviewListIcon

            // pin icon
            binding.ivPin.drawable.setTint(note.getColor(c))
            binding.ivPin.isGone = !note.isPinned
        }
    }
}

private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem == newItem && oldItem.isSelected == newItem.isSelected
}

private fun LocalDateTime.formatNoteItem(): String {
    val today = LocalDate.now().atStartOfDay()
    val thisYear = LocalDate.now().atStartOfYear()

    return format(
        when {
            isAfter(today) -> FORMATTER_TODAY
            isAfter(thisYear) -> FORMATTER_THIS_YEAR
            else -> FORMATTER_EARLIER
        }
    )
}