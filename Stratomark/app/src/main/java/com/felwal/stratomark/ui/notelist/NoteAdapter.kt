package com.felwal.stratomark.ui.notelist

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felwal.stratomark.R
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.databinding.ItemNoteBinding

class NoteAdapter(
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : ListAdapter<Note, RecyclerView.ViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNoteBinding.inflate(inflater)

        return NoteViewHolder(parent.context, binding, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = getItem(position)
        (holder as NoteViewHolder).bind(note)
    }

    class NoteViewHolder(
        private val c: Context,
        binding: ItemNoteBinding,
        val onClick: (Note) -> Unit,
        val onLongClick: (Note) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val tvUri: TextView = binding.tvUri
        private val tvTitle: TextView = binding.tvTitle
        private val tvBody: TextView = binding.tvBody
        private val clBackground: ConstraintLayout = binding.clNote
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

            tvUri.text = note.uri
            tvTitle.text = note.filename
            tvBody.text = note.content.trim()

            // mark selected
            clBackground.background.setTintMode(PorterDuff.Mode.SRC_OVER)
            if (note.selected) clBackground.background.setTint(c.getColor(R.color.red_light_trans))
            else clBackground.background.setTint(c.getColor(R.color.trans))
        }
    }
}

private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem == newItem && oldItem.selected == newItem.selected
}