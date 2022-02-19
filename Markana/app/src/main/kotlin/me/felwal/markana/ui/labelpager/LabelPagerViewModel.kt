package me.felwal.markana.ui.labelpager

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.felwal.markana.data.Label
import me.felwal.markana.data.Note
import me.felwal.markana.data.Repository
import me.felwal.markana.ui.labelpager.notelist.NoteListViewModel

class LabelPagerViewModel(private val repo: Repository) : ViewModel() {

    val labelsData by lazy {
        MutableLiveData<MutableList<Label>>()
    }

    var noteListViewModels = listOf<NoteListViewModel>()

    val labels: List<Label> get() = labelsData.value ?: listOf()
    val selectedNotes: List<Note> get() = noteListViewModels.map { it.selectedNotes }.flatten()
    val selectionCount: Int get() = noteListViewModels.fold(0) { sum, model -> sum + model.selectionCount }
    val treeSelectionCount: Int get() = selectedNotes.mapNotNull { it.treeId }.toSet().size
    val isSelectionMode: Boolean get() = selectionCount != 0
    val isSelectionPinned: Boolean get() = selectedNotes.all { it.isPinned }
    val isSelectionArchived: Boolean get() = selectedNotes.all { it.isArchived }

    //

    fun notifyAdapters() = forEachModel { it.notifyAdapter() }

    fun notifyManagers() = forEachModel { it.notifyManager() }

    fun setRefreshLayoutsEnabled(enabled: Boolean) = forEachModel { it.srlEnabledData.postValue(enabled) }

    // shallow

    fun selectAllNotes(selectedLabelPosition: Int) = noteListViewModels[selectedLabelPosition].selectAllNotes()

    fun deselectAllNotes() = forEachModel { it.deselectAllNotes() }

    fun searchNotes(query: String) = forEachModel { it.searchNotes(query) }

    // read

    fun loadLables() {
        viewModelScope.launch {
            val labels = repo.getLabels()
            labelsData.postValue(labels.toMutableList())
        }
    }

    fun loadNotes() = forEachModel { it.loadNotes() }

    // write

    fun addLabel(name: String) {
        viewModelScope.launch {
            repo.addLabel(name)
            loadLables()
        }
    }

    fun renameLabel(id: Long, newName: String) {
        viewModelScope.launch {
            repo.renameLabel(id, newName)
            loadLables()
        }
    }

    fun deleteLabel(id: Long) {
        viewModelScope.launch {
            repo.deleteLabel(id)
            // TODO: maybe just delete tab + loadNotes?
            loadLables()
        }
    }

    fun linkNote(openDocumentLauncher: ActivityResultLauncher<Array<String>>) {
        repo.linkNote(openDocumentLauncher)
        loadNotes()
    }

    fun linkFolder(openTreeLauncher: ActivityResultLauncher<Uri>) {
        repo.linkFolder(openTreeLauncher)
        loadNotes()
    }

    fun handleOpenedDocument(uri: Uri) {
        viewModelScope.launch {
            repo.handleOpenedDocument(uri)
            loadNotes()
        }
    }

    fun handleOpenedTree(uri: Uri) {
        viewModelScope.launch {
            repo.handleOpenedDocumentTree(uri)
            loadNotes()
        }
    }

    // write: delegate

    fun pinSelectedNotes() = forEachModel { it.pinSelectedNotes() }

    fun labelSelectedNotes(labelPosition: Int) = forEachModel { it.labelSelectedNotes(labels[labelPosition].id) }

    fun archiveSelectedNotes() = forEachModel { it.archiveSelectedNotes() }

    fun colorSelectedNotes(colorIndex: Int) = forEachModel { it.colorSelectedNotes(colorIndex) }

    fun unlinkSelectedNotes() = forEachModel { it.unlinkSelectedNotes() }

    fun unlinkSelectedTrees() = forEachModel { it.unlinkSelectedTrees() }

    fun deleteSelectedNotes() = forEachModel { it.deleteSelectedNotes() }

    // tool

    private fun forEachModel(action: (NoteListViewModel) -> Unit) = noteListViewModels.forEach(action)
}