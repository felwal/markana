package com.felwal.markana.ui.notelist

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.felwal.markana.App
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.data.prefs.SortBy
import com.felwal.markana.databinding.ActivityNotelistBinding
import com.felwal.markana.prefs
import com.felwal.markana.ui.notedetail.NoteDetailActivity
import com.felwal.markana.ui.setting.SettingsActivity
import com.felwal.markana.util.defaults
import com.felwal.markana.util.empty
import com.felwal.markana.util.getColorByAttr
import com.felwal.markana.util.getDrawableCompat
import com.felwal.markana.util.getInteger
import com.felwal.markana.util.getQuantityString
import com.felwal.markana.util.isPortrait
import com.felwal.markana.util.launchActivity
import com.felwal.markana.util.toggleInclusion
import com.felwal.markana.util.updateTheme
import com.felwal.markana.widget.FabMenu
import com.felwal.markana.widget.dialog.BinaryDialog
import com.felwal.markana.widget.dialog.binaryDialog
import com.google.android.material.appbar.AppBarLayout

private const val LOG_TAG = "NoteList"

private const val DIALOG_DELETE = "deleteNotes"
private const val DIALOG_UNLINK = "unlinkNotes"

class NoteListActivity : AppCompatActivity(), BinaryDialog.DialogListener, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityNotelistBinding
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
        updateTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityNotelistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init tb
        setSupportActionBar(binding.tb)
        val homeIcon = getDrawableCompat(R.drawable.ic_cancel)?.mutate()
        homeIcon?.let {
            it.setColorFilter(getColorByAttr(R.attr.colorControlActivated), PorterDuff.Mode.SRC_IN)
            supportActionBar?.setHomeAsUpIndicator(it)
        }

        initFabMenu()
        initRecycler()
        initRefreshLayout()

        // data
        val container = (application as App).appContainer
        model = container.noteListViewModel

        model.itemsData.observe(this) { items ->
            submitItems(items)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            // set sorting
            findItem(
                when (prefs.sortBy) {
                    SortBy.NAME -> R.id.action_sort_name
                    SortBy.MODIFIED -> R.id.action_sort_modified
                    SortBy.OPENED -> R.id.action_sort_opened
                }
            )?.isChecked = true
            findItem(R.id.action_sort_asc)?.isChecked = prefs.ascending

            // set gridView
            findItem(R.id.action_view_toggle)?.let {
                it.isChecked = prefs.gridView
                if (prefs.gridView) it.setIcon(R.drawable.ic_view_list).setTitle(R.string.action_view_list)
                else it.setIcon(R.drawable.ic_view_grid).setTitle(R.string.action_view_grid)
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (selectionCount) {
            0 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_notelist_tb, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)

                // change scroll flags: scroll and snap tb
                enableToolbarScroll()
                //binding.ab.setExpanded(false, true)
            }
            1 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_notelist_tb_selection_single, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                // change scroll flags: don't scroll tb
                disableToolbarScroll()
                binding.ab.setExpanded(true, false) // anim is not smooth
            }
            else -> {
                menuInflater.inflate(R.menu.menu_notelist_tb_selection_multi, menu)
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
            invalidateOptionsMenu() // update icon
        }

        // normal tb: sort submenu
        R.id.action_sort_name -> {
            prefs.sortByInt = SortBy.NAME.ordinal
            onOptionsRadioItemSelected(item)
        }
        R.id.action_sort_modified -> {
            prefs.sortByInt = SortBy.MODIFIED.ordinal
            onOptionsRadioItemSelected(item)
        }
        R.id.action_sort_opened -> {
            prefs.sortByInt = SortBy.OPENED.ordinal
            onOptionsRadioItemSelected(item)
        }
        R.id.action_sort_asc -> {
            item.isChecked = !item.isChecked
            prefs.ascending = item.isChecked
            model.loadNotes()
        }

        // single selection tb
        //R.id.action_copy -> copySelection()

        // multi selection tb
        android.R.id.home -> emptySelection()
        R.id.action_pin -> pinSelection()
        R.id.action_unlink -> unlinkSelection()
        R.id.action_delete -> deleteSelection()

        else -> super.onOptionsItemSelected(item)
    }

    override fun onRestart() {
        super.onRestart()
        fabMenu.closeMenuIfOpen()
    }

    override fun onStart() {
        super.onStart()
        model.loadNotes()
        onRefresh()
    }

    override fun onBackPressed() = when {
        fabMenu.closeMenuIfOpen() -> {
        }
        selectionMode -> emptySelection()
        else -> super.onBackPressed()
    }

    //

    private fun onOptionsRadioItemSelected(item: MenuItem) {
        item.isChecked = true
        model.loadNotes()
        adapter.notifyDataSetChanged() // rebind to update modified/opened tv
    }

    // SwipeRefreshLayout

    private fun initRefreshLayout() {
        binding.srl.setOnRefreshListener(this)
        binding.srl.setProgressBackgroundColorSchemeColor(getColorByAttr(android.R.attr.colorBackground))
        binding.srl.setColorSchemeColors(getColorByAttr(R.attr.colorControlActivated))
    }

    override fun onRefresh() {
        model.syncNotes {
            binding.srl.isRefreshing = false
        }
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
        val spanCount = if (!prefs.gridView) getInteger(R.integer.quantity_notelist_list_columns)
        else if (isPortrait) getInteger(R.integer.quantity_notelist_grid_columns_portrait)
        else getInteger(R.integer.quantity_notelist_grid_columns_landscape)
        binding.rv.layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun submitItems(items: List<Note>) {
        adapter.submitList(items)

        // toggle empty page
        binding.clEmpty.isGone = items.isNotEmpty()
    }

    // fab

    private fun initFabMenu() {
        fabMenu = FabMenu(this, layoutInflater, binding.root).apply {
            // create note
            addItem(
                getString(R.string.tv_fab_create),
                getDrawableCompat(R.drawable.ic_create)
            ) {
                emptySelection()
                NoteDetailActivity.startActivity(this@NoteListActivity)
            }

            // link note
            addItem(
                getString(R.string.tv_fab_link),
                getDrawableCompat(R.drawable.ic_link)
            ) {
                emptySelection()
                model.linkNote(openDocument)
            }

            // link folder
            addItem(
                getString(R.string.tv_fab_link_folder),
                getDrawableCompat(R.drawable.ic_folder_add)
            ) {
                emptySelection()
                model.linkFolder(openDocumentTree)
            }
        }
    }

    // toolbar

    private fun updateToolbarTitle() {
        if (selectionCount == 0) {
            supportActionBar?.title = getString(R.string.title_activity_notelist)
        }
        else {
            supportActionBar?.title =
                getQuantityString(R.plurals.tb_notelist_title_selection, selectionCount, selectionCount)
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

    // data: convenience

    private fun copySelection() = copyNote(selectedNote)

    private fun pinSelection() = pinNotes(model.selectedNotes)

    private fun unlinkSelection() = binaryDialog(
        title = getString(R.string.dialog_title_unlink_notes),
        message = getString(R.string.dialog_msg_unlink_notes),
        posBtnTxtRes = R.string.dialog_btn_unlink,
        tag = DIALOG_UNLINK
    ).show(supportFragmentManager)

    private fun deleteSelection() = binaryDialog(
        title = getString(R.string.dialog_title_delete_notes),
        message = getString(R.string.dialog_msg_delete_notes),
        posBtnTxtRes = R.string.dialog_btn_delete,
        tag = DIALOG_DELETE
    ).show(supportFragmentManager)

    // data

    private fun copyNote(note: Note?) {
        // TODO: needs createAndSave
        /*note?.let {
            val copy = it.copy(id = NO_ID)
            db.noteDao().addNote(copy)
            emptySelection()
        }*/
    }

    private fun pinNotes(notes: List<Note>) {
        model.pinNotes(notes)
        emptySelection()
    }

    private fun unlinkNotes(notes: List<Note>) {
        model.unlinkNotes(notes)
        emptySelection()
    }

    private fun deleteNotes(notes: List<Note>) {
        model.deleteNotes(notes)
        emptySelection()
    }

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