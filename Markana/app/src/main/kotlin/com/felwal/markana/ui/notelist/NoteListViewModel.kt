package com.felwal.markana.ui.notelist

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.android.util.toggleInclusion
import com.felwal.android.util.withUI
import com.felwal.markana.data.Note
import com.felwal.markana.data.NoteRepository
import kotlinx.coroutines.launch

class NoteListViewModel(private val repo: NoteRepository) : ViewModel() {

    // primary
    val itemsData by lazy { MutableLiveData<MutableList<Note>>() }
    val selectionIndices: MutableList<Int> = mutableListOf()
    private var searchQuery: String = ""

    // secondary
    val items: List<Note> get() = itemsData.value ?: listOf()
    val selectedNotes: List<Note> get() = items.filter { it.isSelected }
    val selectionCount: Int get() = selectionIndices.size
    val treeSelectionCount: Int get() = selectedNotes.mapNotNull { it.treeId }.toSet().size
    val isSelectionMode: Boolean get() = selectionCount != 0
    val isSelectionPinned: Boolean get() = selectedNotes.all { it.isPinned }
    val isSelectionArchived: Boolean get() = selectedNotes.all { it.isArchived }
    val searchQueryOrNull get() = if (isSearching) searchQuery else null
    val isSearching: Boolean get() = searchQuery != ""

    // shallow

    fun searchNotes(query: String) {
        searchQuery = query
        loadNotes()
    }

    fun toggleNoteSelection(note: Note): Int {
        note.isSelected = !note.isSelected

        val index = items.indexOf(note)
        itemsData.value?.set(index, note)
        selectionIndices.toggleInclusion(index)

        return index
    }

    // read

    fun loadNotes() {
        viewModelScope.launch {
            val notes = repo.getNotes(searchQuery)
                .toMutableList()
                .onEachIndexed { index, note ->
                    // sync with selection
                    note.isSelected = index in selectionIndices
                }

            itemsData.postValue(notes)
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

    fun linkNote(openDocumentLauncher: ActivityResultLauncher<Array<String>>) {
        repo.linkNote(openDocumentLauncher)
        loadNotes()
    }

    fun linkFolder(openTreeLauncher: ActivityResultLauncher<Uri>) {
        repo.linkFolder(openTreeLauncher)
        loadNotes()
    }

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

    fun handleOpenedDocument(uri: Uri) {
        viewModelScope.launch {
            repo.handleOpenedDocument(uri)
            loadNotes()
        }
    }

    fun handleOpenedTree(uri: Uri) {
        viewModelScope.launch {
            repo.handleOpenedDocumentTree(uri)
            loadNotes()
        }
    }
}