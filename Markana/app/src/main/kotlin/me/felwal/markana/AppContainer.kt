package me.felwal.markana

import android.content.Context
import me.felwal.markana.data.Label
import me.felwal.markana.data.Repository
import me.felwal.markana.data.db.DbDataSource
import me.felwal.markana.data.network.SafDataSource
import me.felwal.markana.ui.labelpager.LabelPagerViewModel
import me.felwal.markana.ui.labelpager.notelist.NoteListViewModel
import me.felwal.markana.ui.notedetail.NoteDetailContainer

class AppContainer(applicationContext: Context) {

    // container
    var noteDetailContainer: NoteDetailContainer? = null

    // data source
    private val db = DbDataSource.getInstance(applicationContext)
    private val saf = SafDataSource(applicationContext)

    // repository
    val repository = Repository(applicationContext, db, saf)

    // viewmodel
    val labelPagerViewModel = LabelPagerViewModel(repository)
    var noteListViewModels = listOf<NoteListViewModel>()

    //

    fun createNoteListViewModels(labels: List<Label>) {
        noteListViewModels = labels.map { label -> NoteListViewModel(repository, labelPagerViewModel, label) }
        labelPagerViewModel.noteListViewModels = noteListViewModels
    }
}