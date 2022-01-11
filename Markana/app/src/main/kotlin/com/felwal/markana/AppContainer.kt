package com.felwal.markana

import android.content.Context
import com.felwal.markana.data.Label
import com.felwal.markana.data.NoteRepository
import com.felwal.markana.data.db.DbDataSource
import com.felwal.markana.data.network.SafDataSource
import com.felwal.markana.ui.notedetail.NoteDetailContainer
import com.felwal.markana.ui.labelpager.LabelPagerViewModel
import com.felwal.markana.ui.labelpager.notelist.NoteListViewModel

class AppContainer(applicationContext: Context) {

    // container
    var noteDetailContainer: NoteDetailContainer? = null

    // data source
    private val db = DbDataSource.getInstance(applicationContext)
    private val saf = SafDataSource(applicationContext)

    // repository
    val noteRepository = NoteRepository(applicationContext, db, saf)

    // viewmodel
    val labelPagerViewModel = LabelPagerViewModel(noteRepository)
    var noteListViewModels = listOf<NoteListViewModel>()

    //

    fun createNoteListViewModels(labels: List<Label>) {
        noteListViewModels = labels.map { label -> NoteListViewModel(noteRepository, labelPagerViewModel, label) }
        labelPagerViewModel.noteListViewModels = noteListViewModels
    }
}