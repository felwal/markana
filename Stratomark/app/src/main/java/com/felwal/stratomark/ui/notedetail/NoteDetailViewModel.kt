package com.felwal.stratomark.ui.notedetail

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.data.NoteRepository
import kotlinx.coroutines.launch

class NoteDetailViewModel(private val repo: NoteRepository) : ViewModel() {

    val noteData: MutableLiveData<Note> by lazy {
        MutableLiveData<Note>()
    }
    var noteUri: String? = null

    // read

    fun loadNote() {
        noteUri?.let {
            viewModelScope.launch {
                noteData.value = repo.getNote(it)
            }
        }
    }

    // write

    fun createNote(resultLauncher: ActivityResultLauncher<String>) {
        viewModelScope.launch {
            repo.createNote(resultLauncher)
            //loadNote()
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