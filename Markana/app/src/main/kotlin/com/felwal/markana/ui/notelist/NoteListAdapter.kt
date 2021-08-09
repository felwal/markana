package com.felwal.markana.ui.notelist

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.databinding.ItemNotelistGridNoteBinding
import com.felwal.markana.databinding.ItemNotelistListNoteBinding
import com.felwal.markana.prefs
import com.felwal.markana.data.prefs.SortBy
import com.felwal.markana.util.FORMATTER_EARLIER
import com.felwal.markana.util.FORMATTER_THIS_YEAR
import com.felwal.markana.util.FORMATTER_TODAY
import com.felwal.markana.util.atStartOfYear
import com.felwal.markana.util.fromEpochSecond
import java.time.LocalDate
import java.time.LocalDateTime

class NoteListAdapter(
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : ListAdapter<Note, RecyclerView.ViewHolder>(NoteDiffCallback()) {

    fun invertViewType() {
        prefs.gridView = !prefs.gridView
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (prefs.gridView) {
            val binding = ItemNotelistGridNoteBinding.inflate(inflater)
            GridNoteViewHolder(parent.context, binding, onClick, onLongClick)
        }
        else {
            val binding = ItemNotelistListNoteBinding.inflate(inflater)
            ListNoteViewHolder(parent.context, binding, onClick, onLongClick)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = getItem(position)
        if (holder is GridNoteViewHolder) holder.bind(note)
        else if (holder is ListNoteViewHolder) holder.bind(note)
    }

    // viewholder

    class GridNoteViewHolder(
        c: Context,
        private val binding: ItemNotelistGridNoteBinding,
        private val onClick: (Note) -> Unit,
        private val onLongClick: (Note) -> Unit
    ) : SelectableViewHolder(c, binding, binding.clNote) {

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

            binding.tvUri.text = note.uri
            binding.tvTitle.text = note.filename
            binding.tvBody.text = note.content.trim() // TODO: dont get the full string

            // mark selected and pinned
            markSelection(note.isSelected)
            binding.ivPin.isInvisible = !note.isPinned
        }
    }

    class ListNoteViewHolder(
        c: Context,
        private val binding: ItemNotelistListNoteBinding,
        private val onClick: (Note) -> Unit,
        private val onLongClick: (Note) -> Unit
    ) : SelectableViewHolder(c, binding, binding.clNote) {

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

            binding.tvTitle.text = note.filename
            binding.tvModified.text =
                if (prefs.sortBy == SortBy.OPENED) {
                    "Opened ${note.opened?.fromEpochSecond()?.formatNoteItem() ?: "never"}"
                }
                else {
                    "Modified ${note.modified?.fromEpochSecond()?.formatNoteItem() ?: "never"}"
                }

            // mark selected and pinned
            markSelection(note.isSelected)
            binding.ivPin.isInvisible = !note.isPinned
        }
    }

    abstract class SelectableViewHolder(
        private val c: Context,
        binding: ViewBinding,
        private var selectableBackground: ConstraintLayout
    ) : RecyclerView.ViewHolder(binding.root) {

        protected fun markSelection(selected: Boolean) {
            selectableBackground.background.setTintMode(PorterDuff.Mode.SRC_OVER)
            if (selected) selectableBackground.background.setTint(c.getColor(R.color.red_light_trans))
            else selectableBackground.background.setTint(c.getColor(R.color.trans))
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