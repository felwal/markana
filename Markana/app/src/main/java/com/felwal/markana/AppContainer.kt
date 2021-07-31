package com.felwal.markana

import android.content.Context
import com.felwal.markana.data.AppDatabase
import com.felwal.markana.data.NoteRepository
import com.felwal.markana.network.SafHelper
import com.felwal.markana.ui.notedetail.NoteDetailContainer
import com.felwal.markana.ui.notelist.NoteListViewModel

class AppContainer(applicationContext: Context) {

    // container
    var noteDetailContainer: NoteDetailContainer? = null

    // data source
    private val db = AppDatabase.getInstance(applicationContext)
    private val saf = SafHelper(applicationContext)

    // repository
    val noteRepository = NoteRepository(applicationContext, db, saf)

    // viewmodel
    val noteListViewModel = NoteListViewModel(noteRepository)
}