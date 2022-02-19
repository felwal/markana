package com.felwal.markana.ui.labelpager.notelist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.markana.data.Label
import com.felwal.markana.data.Note
import com.felwal.markana.data.NoteRepository
import com.felwal.markana.ui.labelpager.LabelPagerViewModel
import kotlinx.coroutines.launch
import me.felwal.android.util.removeAll
import me.felwal.android.util.toggleInclusion
import me.felwal.android.util.withUI

class NoteListViewModel(
    private val repo: NoteRepository,
    private val labelPagerViewModel: LabelPagerViewModel,
    val label: Label
) : ViewModel() {

    val notesData by lazy {
        MutableLiveData<MutableList<Note>>()
    }
    val notifyAdapterData by lazy {
        MutableLiveData<Boolean>().apply {
            postValue(false)
        }
    }
    val notifyManagerData by lazy {
        MutableLiveData<Boolean>().apply {
            postValue(false)
        }
    }
    val srlEnabledData by lazy {
        MutableLiveData<Boolean>().apply {
            postValue(true)
        }
    }

    private val selectionIndices: MutableList<Int> = mutableListOf()
    private var searchQuery: String = ""

    val notes: List<Note> get() = notesData.value ?: listOf()
    val selectedNotes: List<Note> get() = notes.filter { it.isSelected }
    val selectionCount: Int get() = selectionIndices.size
    val isSelectionMode: Boolean get() = labelPagerViewModel.isSelectionMode
    val searchQueryOrNull get() = if (isSearching) searchQuery else null
    val isSearching: Boolean get() = searchQuery.isNotEmpty()

    // shallow

    fun notifyAdapter() = notifyAdapterData.postValue(true)

    fun notifyManager() = notifyManagerData.postValue(true)

    fun searchNotes(query: String) {
        searchQuery = query
        loadNotes()
    }

    fun toggleNoteSelection(note: Note): Int {
        note.isSelected = !note.isSelected

        val index = notes.indexOf(note)
        notesData.value?.set(index, note)
        selectionIndices.toggleInclusion(index)

        return index
    }

    fun selectAllNotes() {
        for (note in notes) {
            if (note.isSelected) continue

            // sync with data and adapter
            val index = toggleNoteSelection(note)
            //listAdapter.notifyItemChanged(index)
        }
        notifyAdapter()
    }

    fun deselectAllNotes() {
        for (note in selectedNotes) {
            note.isSelected = false

            val index = notes.indexOf(note)
            notesData.value?.set(index, note)
            //listAdapter.notifyItemChanged(index)
        }
        selectionIndices.removeAll()
        notifyAdapter()
    }

    // read

    fun loadNotes() {
        viewModelScope.launch {
            val notes = repo.getNotes(label.id, searchQuery)
                .toMutableList()
                .onEachIndexed { index, note ->
                    // sync with selection
                    note.isSelected = index in selectionIndices
                }
            notesData.postValue(notes)
        }
    }

    fun syncNotes(onFinished: () -> Unit) {
        viewModelScope.launch {
            repo.syncNotes()
            loadNotes()
            withUI {
                onFinished()
            }
        }
    }

    // write

    fun pinSelectedNotes() {
        viewModelScope.launch {
            val notes = selectedNotes
            // determine if to unpin all or pin those not already pinned
            val unpin = notes.all { it.isPinned }
            // apply
            for (note in notes) {
                note.isPinned = !unpin
                // also unarchive to avoid confusing combination of pinned and archived
                if (note.isPinned) note.isArchived = false
            }

            repo.updateNoteMetadata(*notes.toTypedArray())
            loadNotes()
        }
    }

    fun labelSelectedNotes(labelId: Long) {
        viewModelScope.launch {
            val notes = selectedNotes

            for (note in notes) note.labelId = labelId

            repo.updateNoteMetadata(*notes.toTypedArray())
            loadNotes()
        }
    }

    fun archiveSelectedNotes() {
        viewModelScope.launch {
            val notes = selectedNotes
            // determine if to unarchive all or archive those not already archived
            val unarchive = notes.all { it.isArchived }
            // apply
            for (note in notes) {
                note.isArchived = !unarchive
                // also unpin to avoid confusing combination of pinned and archived
                if (note.isArchived) note.isPinned = false
            }

            repo.updateNoteMetadata(*notes.toTypedArray())
            loadNotes()
        }
    }

    fun colorSelectedNotes(colorIndex: Int) {
        viewModelScope.launch {
            val notes = selectedNotes
            for (note in notes) note.colorIndex = colorIndex

            repo.updateNoteMetadata(*notes.toTypedArray())
            loadNotes()
        }
    }

    fun unlinkSelectedNotes() {
        viewModelScope.launch {
            selectedNotes.map { it.uri }.forEach {
                repo.unlinkNote(it)
            }
            loadNotes()
        }
    }

    fun unlinkSelectedTrees() {
        viewModelScope.launch {
            selectedNotes.mapNotNull { it.treeId }.forEach {
                repo.unlinkTree(it)
            }
            loadNotes()
        }
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            selectedNotes.map { it.uri }.forEach {
                repo.deleteNote(it)
            }
            loadNotes()
        }
    }
}