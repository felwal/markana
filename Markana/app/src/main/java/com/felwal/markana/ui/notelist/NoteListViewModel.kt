package com.felwal.markana.ui.notelist

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.markana.data.Note
import com.felwal.markana.data.NoteRepository
import kotlinx.coroutines.launch

class NoteListViewModel(private val repo: NoteRepository) : ViewModel() {

    val itemsData: MutableLiveData<MutableList<Note>> by lazy {
        MutableLiveData<MutableList<Note>>()
    }
    val selectionIndices: MutableList<Int> = mutableListOf()

    val items: List<Note> get() = itemsData.value ?: listOf()
    val selectedNotes: List<Note> get() = items.filter { it.selected }

    // read

    fun loadNotes() {
        viewModelScope.launch {
            itemsData.postValue(repo.getAllNotes().toMutableList())

            // sync with selection
            itemsData.value?.onEachIndexed { index, note ->
                note.selected = index in selectionIndices
            }
        }
    }

    // write

    fun linkNote(resultLauncher: ActivityResultLauncher<Array<String>>) {
        viewModelScope.launch {
            repo.linkNote(resultLauncher)
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
}