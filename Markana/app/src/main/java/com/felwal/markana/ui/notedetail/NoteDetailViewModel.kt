package com.felwal.markana.ui.notedetail

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.markana.data.Note
import com.felwal.markana.data.NoteRepository
import kotlinx.coroutines.launch

class NoteDetailViewModel(private val repo: NoteRepository) : ViewModel() {

    val noteData: MutableLiveData<Note> by lazy {
        MutableLiveData<Note>()
    }
    val note: Note get() = noteData.value!!

    var noteUri: String? = null

    // read

    fun loadNote() {
        noteUri?.let {
            viewModelScope.launch {
                noteData.postValue(repo.getNote(it))
            }
        }
    }

    // write

    /**
     * [createDocumentLauncher] should call [loadNote].
     */
    fun createNote(createDocumentLauncher: ActivityResultLauncher<String>) {
        viewModelScope.launch {
            repo.createNote(createDocumentLauncher)
        }
    }

    fun saveNote(note: Note, rename: Boolean) {
        viewModelScope.launch {
            repo.saveNote(note, rename)
            loadNote()
        }
    }

    fun deleteNote() {
        noteUri?.let {
            viewModelScope.launch {
                repo.deleteNote(it)
                loadNote()
            }
        }
    }

    fun persistNotePermissions(uri: Uri) {
        viewModelScope.launch {
            repo.persistPermissions(uri)
        }
    }
}