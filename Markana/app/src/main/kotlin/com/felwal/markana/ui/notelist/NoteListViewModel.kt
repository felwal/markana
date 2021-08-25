package com.felwal.markana.ui.notelist

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.markana.data.Note
import com.felwal.markana.data.NoteRepository
import com.felwal.markana.util.withUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteListViewModel(private val repo: NoteRepository) : ViewModel() {

    val itemsData by lazy { MutableLiveData<MutableList<Note>>() }
    val selectionIndices: MutableList<Int> = mutableListOf()

    val items: List<Note> get() = itemsData.value ?: listOf()
    val selectedNotes: List<Note> get() = items.filter { it.isSelected }

    // read

    fun loadNotes() {
        viewModelScope.launch {
            val notes = repo.getNotes()
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

    fun pinNotes(notes: List<Note>) {
        viewModelScope.launch {
            // determine if to unpin all or pin those not already pinned
            val unpinAll = notes.all { it.isPinned }
            // apply
            for (note in notes) note.isPinned = !unpinAll

            repo.updateNoteMetadata(*notes.toTypedArray())
            loadNotes()
        }
    }

    fun colorNotes(notes: List<Note>, colorIndex: Int) {
        viewModelScope.launch {
            for (note in notes) note.colorIndex = colorIndex

            repo.updateNoteMetadata(*notes.toTypedArray())
            loadNotes()
        }
    }

    fun unlinkNotes(notes: List<Note>) {
        viewModelScope.launch {
            repo.unlinkNotes(notes.map { it.uri })
            loadNotes()
        }
    }

    fun deleteNotes(notes: List<Note>) {
        viewModelScope.launch {
            repo.deleteNotes(notes.map { it.uri })
            loadNotes()
        }
    }

    fun handleCreatedNote(uri: Uri) {
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