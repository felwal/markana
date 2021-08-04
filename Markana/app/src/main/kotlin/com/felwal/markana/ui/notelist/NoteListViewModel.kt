package com.felwal.markana.ui.notelist

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.markana.data.Note
import com.felwal.markana.data.NoteRepository
import com.felwal.markana.util.withUI
import kotlinx.coroutines.launch

class NoteListViewModel(private val repo: NoteRepository) : ViewModel() {

    val itemsData: MutableLiveData<MutableList<Note>> by lazy {
        MutableLiveData<MutableList<Note>>()
    }
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
        viewModelScope.launch {
            repo.linkNote(openDocumentLauncher)
            loadNotes()
        }
    }

    fun linkFolder(openTreeLauncher: ActivityResultLauncher<Uri>) {
        viewModelScope.launch {
            repo.linkFolder(openTreeLauncher)
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