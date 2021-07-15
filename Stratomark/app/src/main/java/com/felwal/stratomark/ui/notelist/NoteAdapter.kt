package com.felwal.stratomark.ui.notelist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.databinding.ItemNoteBinding

class NoteAdapter(
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : ListAdapter<Note, RecyclerView.ViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNoteBinding.inflate(inflater)

        return NoteViewHolder(binding, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = getItem(position)
        (holder as NoteViewHolder).bind(note)
    }

    class NoteViewHolder(
        binding: ItemNoteBinding,
        val onClick: (Note) -> Unit,
        val onLongClick: (Note) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val tvTitle: TextView = binding.tvTitle
        private val tvBody: TextView = binding.tvBody
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

            tvTitle.text = note.titleWithExt
            tvBody.text = note.body.trim()
        }
    }
}

private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem.noteId == newItem.noteId

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem == newItem
}