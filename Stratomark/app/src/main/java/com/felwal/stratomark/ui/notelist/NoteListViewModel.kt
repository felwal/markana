package com.felwal.stratomark.ui.notelist

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.data.NoteRepository
import kotlinx.coroutines.launch

class NoteListViewModel(private val repo: NoteRepository) : ViewModel() {

    var items: MutableList<Note> = mutableListOf()
    val selectionIndices: MutableList<Int> = mutableListOf()

    val selectedNotes: List<Note>
        get() = items.onEachIndexed { index, note ->
            note.selected = index in selectionIndices
        }.filter { it.selected }.toMutableList()

    lateinit var writeCallback: () -> Unit

    //

    fun getNotes(callback: (notes: List<Note>) -> Unit) {
        viewModelScope.launch {
            val notes = repo.getAllNotes().toMutableList()
            items = notes
            callback(notes)
        }
    }

    fun linkNote(resultLauncher: ActivityResultLauncher<Array<String>>) {
        viewModelScope.launch {
            repo.linkNote(resultLauncher)
            writeCallback()
        }
    }

    fun unlinkNotes(notes: List<Note>, callback: () -> Unit) {
        viewModelScope.launch {
            repo.unlinkNotes(notes.map { it.uri })
            callback()
            writeCallback()
        }
    }

    fun deleteNotes(notes: List<Note>, callback: () -> Unit) {
        viewModelScope.launch {
            repo.deleteNotes(notes.map { it.uri })
            callback()
            writeCallback()
        }
    }

    fun handleOpenedDocument(uri: Uri) {
        viewModelScope.launch {
            repo.handleDocumentOpened(uri)
        }
    }
}