package com.felwal.markana.ui.notelist

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.felwal.markana.MainApplication
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.databinding.ActivityNoteListBinding
import com.felwal.markana.dialog.BinaryDialog
import com.felwal.markana.dialog.binaryDialog
import com.felwal.markana.ui.notedetail.NoteDetailActivity
import com.felwal.markana.ui.setting.SettingsActivity
import com.felwal.markana.util.defaults
import com.felwal.markana.util.empty
import com.felwal.markana.util.getAttrColor
import com.felwal.markana.util.isPortrait
import com.felwal.markana.util.launchActivity
import com.felwal.markana.util.showOrRemove
import com.felwal.markana.util.toggleInclusion
import com.felwal.markana.view.FabMenu
import com.google.android.material.appbar.AppBarLayout

private const val LOG_TAG = "NoteList"

private const val DIALOG_DELETE = "deleteNotes"
private const val DIALOG_UNLINK = "unlinkNotes"

class NoteListActivity : AppCompatActivity(), BinaryDialog.DialogListener {

    private lateinit var binding: ActivityNoteListBinding
    private lateinit var adapter: NoteListAdapter

    private lateinit var model: NoteListViewModel

    private val selectionCount: Int get() = model.selectionIndices.size
    private val selectionMode: Boolean get() = selectionCount != 0
    private val selectedNote: Note? get() = if (selectionCount > 0) model.selectedNotes[0] else null

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        Log.i(LOG_TAG, "open document uri result: $uri")

        model.handleCreatedNote(uri)
    }
    private val openDocumentTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@registerForActivityResult
        Log.i(LOG_TAG, "open document tree uri result: $uri")

        model.handleOpenedTree(uri)
    }

    private lateinit var fabMenu: FabMenu

    // Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tb)

        // tb: deselect as home
        val homeIcon = ContextCompat.getDrawable(this, R.drawable.ic_cancel)?.mutate()
        homeIcon?.let {
            it.setColorFilter(getAttrColor(R.attr.colorControlActivated), PorterDuff.Mode.SRC_IN)
            supportActionBar?.setHomeAsUpIndicator(it)
        }

        initFabMenu()
        initRecycler()

        // data
        val container = (application as MainApplication).appContainer
        model = container.noteListViewModel

        model.itemsData.observe(this) { items ->
            submitItems(items)
        }
        model.loadNotes()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            // toggle item view
            val viewToggleItem = menu.findItem(R.id.action_view_toggle)
            viewToggleItem?.let {
                viewToggleItem.isChecked = adapter.gridView
                if (adapter.gridView) viewToggleItem.setIcon(R.drawable.ic_view_list).setTitle(R.string
                    .action_view_list)
                else viewToggleItem.setIcon(R.drawable.ic_view_grid).setTitle(R.string.action_view_grid)
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (selectionCount) {
            0 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_note_list_tb, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)

                // change scroll flags: scroll and snap tb
                enableToolbarScroll()
                //binding.ab.setExpanded(false, true)
            }
            1 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_note_list_tb_selection_single, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                // change scroll flags: don't scroll tb
                disableToolbarScroll()
                binding.ab.setExpanded(true, false) // anim is not smooth
            }
            else -> {
                menuInflater.inflate(R.menu.menu_note_list_tb_selection_multi, menu)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = true defaults when (item.itemId) {
        // normal tb
        R.id.action_settings -> launchActivity<SettingsActivity>()
        R.id.action_view_toggle -> {
            adapter.invertViewType()
            setAdapterAndManager()
            invalidateOptionsMenu()
        }

        // single selection tb
        R.id.action_copy -> copySelection()

        // multi selection tb
        android.R.id.home -> emptySelection()
        R.id.action_unlink -> unlinkSelection()
        R.id.action_delete -> deleteSelection()

        else -> super.onOptionsItemSelected(item)
    }

    override fun onRestart() {
        super.onRestart()
        model.loadNotes()
        fabMenu.closeMenuIfOpen()
    }


    override fun onBackPressed() = when {
        fabMenu.closeMenuIfOpen() -> {}
        selectionMode -> emptySelection()
        else -> super.onBackPressed()
    }

    // recycler

    private fun initRecycler() {
        // animate tb and fab on scroll
        binding.rv.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // animate tb
            binding.ab.isSelected = binding.rv.canScrollVertically(-1)

            // show/hide fab
            fabMenu.showHideOnScroll(scrollY - oldScrollY)
        }

        // adapter
        adapter = NoteListAdapter(
            onClick = {
                if (selectionMode) selectNote(it)
                else NoteDetailActivity.startActivity(this, it.uri)
            },
            onLongClick = {
                selectNote(it)
            }
        )
        setAdapterAndManager()
    }

    private fun setAdapterAndManager() {
        // set adapter
        binding.rv.adapter = adapter

        // set manager
        val spanCount = if (!adapter.gridView) 1 else if (isPortrait) 2 else 3
        binding.rv.layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun submitItems(items: List<Note>) {
        adapter.submitList(items)

        // toggle empty page
        binding.clEmpty.showOrRemove(items.isEmpty())
    }

    // fab

    private fun initFabMenu() {
        fabMenu = FabMenu(this, layoutInflater, binding.root)

        // create note
        fabMenu.addItem(
            getString(R.string.tv_fab_create),
            ContextCompat.getDrawable(this, R.drawable.ic_create)
        ) {
            emptySelection()
            NoteDetailActivity.startActivity(this)
        }

        // link note
        fabMenu.addItem(
            getString(R.string.tv_fab_link),
            ContextCompat.getDrawable(this, R.drawable.ic_link)
        ) {
            emptySelection()
            model.linkNote(openDocument)
        }

        // link folder
        fabMenu.addItem(
            getString(R.string.tv_fab_link_folder),
            ContextCompat.getDrawable(this, R.drawable.ic_folder_add)
        ) {
            emptySelection()
            model.linkFolder(openDocumentTree)
        }
    }

    // toolbar

    private fun updateToolbarTitle() {
        if (selectionCount == 0) {
            supportActionBar?.title = getString(R.string.title_activity_note_list)
        }
        else {
            supportActionBar?.title =
                resources.getQuantityString(R.plurals.tb_notelist_title_selection, selectionCount, selectionCount)
        }
    }

    private fun enableToolbarScroll() {
        val params: AppBarLayout.LayoutParams = binding.ctb.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        binding.ctb.layoutParams = params
    }

    private fun disableToolbarScroll() {
        val params: AppBarLayout.LayoutParams = binding.ctb.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        binding.ctb.layoutParams = params
    }

    // data

    private fun copyNote(note: Note?) {
        // TODO: needs createAndSave
        /*note?.let {
            val copy = it.copy(id = NO_ID)
            db.noteDao().addNote(copy)
            emptySelection()
        }*/
    }

    private fun unlinkNotes(notes: List<Note>) {
        model.unlinkNotes(notes)
        emptySelection()
    }

    private fun deleteNotes(notes: List<Note>) {
        model.deleteNotes(notes)
        emptySelection()
    }

    // data: convenience

    private fun copySelection() = copyNote(selectedNote)

    private fun deleteSelection() = binaryDialog(
        title = getString(R.string.tv_settings_item_title_delete_notes),
        message = getString(R.string.dialog_msg_delete_notes),
        posBtnTxtRes = R.string.dialog_btn_delete,
        tag = DIALOG_DELETE
    ).show(supportFragmentManager)

    private fun unlinkSelection() = binaryDialog(
        title = getString(R.string.tv_settings_item_title_unlink_notes),
        message = getString(R.string.dialog_msg_unlink_notes),
        posBtnTxtRes = R.string.dialog_btn_unlink,
        tag = DIALOG_UNLINK
    ).show(supportFragmentManager)

    // selection

    private fun selectNote(note: Note) {
        note.isSelected = !note.isSelected

        // sync with data and adapter
        val index = model.items.indexOf(note)
        model.itemsData.value?.set(index, note)
        model.selectionIndices.toggleInclusion(index)
        adapter.notifyItemChanged(index)

        // selection mode just turned off/single/multi; sync tb
        if (selectionCount in 0..2) invalidateOptionsMenu()
        updateToolbarTitle()
    }

    private fun emptySelection() {
        for (note in model.selectedNotes) {
            note.isSelected = false

            val index = model.items.indexOf(note)
            model.itemsData.value?.set(index, note)
            adapter.notifyItemChanged(index)
        }
        model.selectionIndices.empty()

        // sync tb
        invalidateOptionsMenu()
        updateToolbarTitle()
    }

    // dialog

    override fun onBinaryDialogPositiveClick(passValue: String?, tag: String) {
        when (tag) {
            DIALOG_DELETE -> deleteNotes(model.selectedNotes)
            DIALOG_UNLINK -> unlinkNotes(model.selectedNotes)
        }
    }
}