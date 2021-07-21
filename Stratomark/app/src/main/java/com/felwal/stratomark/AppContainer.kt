package com.felwal.stratomark

import android.content.Context
import com.felwal.stratomark.data.AppDatabase
import com.felwal.stratomark.data.NoteRepository
import com.felwal.stratomark.network.SafHelper
import com.felwal.stratomark.ui.notedetail.NoteDetailViewModel
import com.felwal.stratomark.ui.notelist.NoteListViewModel

class AppContainer(applicationContext: Context) {

    // data source
    private val db = AppDatabase.getInstance(applicationContext)
    private val saf = SafHelper(applicationContext)

    // repository
    private val noteRepository = NoteRepository(applicationContext, db, saf)

    // viewmodel
    val noteListViewModel = NoteListViewModel(noteRepository)
    val noteDetailViewModel = NoteDetailViewModel(noteRepository)
}