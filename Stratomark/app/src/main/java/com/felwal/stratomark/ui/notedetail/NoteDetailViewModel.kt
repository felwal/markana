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

    var noteUri: String? = null

    //

    fun getNote(callback: (note: Note?) -> Unit) {
        noteUri?.let {
            viewModelScope.launch {
                val note = repo.getNote(it)
                callback(note)
            }
        }
    }

    fun createNote(resultLauncher: ActivityResultLauncher<String>) {
        viewModelScope.launch {
            repo.createNote(resultLauncher)
        }
    }

    fun saveNote(note: Note, rename: Boolean) {
        viewModelScope.launch {
            repo.saveNote(note, rename)
        }
    }

    fun deleteNote() {
        noteUri?.let {
            viewModelScope.launch {
                repo.deleteNote(it)
            }
        }
    }

    fun persistPermissions(uri: Uri) {
        viewModelScope.launch {
            repo.persistPermissions(uri)
        }
    }
}