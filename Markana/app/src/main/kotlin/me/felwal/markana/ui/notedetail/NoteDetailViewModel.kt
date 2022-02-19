package me.felwal.markana.ui.notedetail

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.felwal.android.util.findAll
import me.felwal.markana.data.Note
import me.felwal.markana.data.Repository

class NoteDetailViewModel(private val repo: Repository) : ViewModel() {

    // primary
    val noteData by lazy { MutableLiveData<Note>() }
    var noteUri: String? = null
    var isFindInNoteMode = false
    var findInNoteLastQuery = ""
    var findInNoteQueryLength = 0
    var findInNoteOccurrenceIndices = listOf<Int>()
    private var findInNoteOccurrenceIndicesCursor = 0

    // secondary
    val note: Note get() = noteData.value!!
    val findInNoteOccurrenceIndex: Int
        get() = findInNoteOccurrenceIndices[findInNoteOccurrenceIndicesCursor]

    // shallow

    fun findInNote(query: String, content: String) {
        findInNoteLastQuery = query
        findInNoteQueryLength = query.length
        findInNoteOccurrenceIndices = content.findAll(query, ignoreCase = true)
        findInNoteOccurrenceIndicesCursor = 0
    }

    fun decFindInNoteCursor() {
        if (findInNoteOccurrenceIndicesCursor > 0) findInNoteOccurrenceIndicesCursor -= 1
        else findInNoteOccurrenceIndicesCursor = findInNoteOccurrenceIndices.size - 1
    }

    fun incFindInNoteCursor() {
        if (findInNoteOccurrenceIndicesCursor < findInNoteOccurrenceIndices.size - 1) findInNoteOccurrenceIndicesCursor += 1
        else findInNoteOccurrenceIndicesCursor = 0
    }

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
        repo.createNote(createDocumentLauncher)
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            val rename = noteData.value?.let { note.filename != it.filename } ?: false
            repo.saveNote(note, rename)
            loadNote()
        }
    }

    private fun syncNote(uri: String) {
        viewModelScope.launch {
            repo.syncNote(uri)
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

    fun handleCreatedDocument(uri: Uri) {
        // save the note
        repo.persistPermissions(uri)
        saveNote(Note(uri = uri.toString()))

        // sync to put filename in db before loading
        syncNote(uri.toString())

        // load the note
        noteUri = uri.toString()
        loadNote()
    }
}