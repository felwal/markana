package com.felwal.markana.ui.notelist

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.felwal.android.util.closeIcon
import com.felwal.android.util.common
import com.felwal.android.util.enableActionItemRipple
import com.felwal.android.util.getColorByAttr
import com.felwal.android.util.getDrawableCompat
import com.felwal.android.util.getDrawableCompatWithTint
import com.felwal.android.util.getInteger
import com.felwal.android.util.getIntegerArray
import com.felwal.android.util.getQuantityString
import com.felwal.android.util.isPortrait
import com.felwal.android.util.launchActivity
import com.felwal.android.util.removeAll
import com.felwal.android.util.searchView
import com.felwal.android.util.setOptionalIconsVisible
import com.felwal.android.widget.dialog.AlertDialog
import com.felwal.android.widget.dialog.SingleChoiceDialog
import com.felwal.android.widget.dialog.alertDialog
import com.felwal.android.widget.dialog.colorDialog
import com.felwal.markana.App
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.data.prefs.SortBy
import com.felwal.markana.databinding.ActivityNotelistBinding
import com.felwal.markana.prefs
import com.felwal.markana.ui.notedetail.NoteDetailActivity
import com.felwal.markana.ui.setting.SettingsActivity
import com.felwal.markana.util.submitListKeepScroll
import com.felwal.markana.util.updateDayNight
import com.felwal.markana.widget.FabMenu
import com.google.android.material.appbar.AppBarLayout

private const val LOG_TAG = "NoteList"

private const val DIALOG_DELETE = "deleteNotes"
private const val DIALOG_UNLINK = "unlinkNotes"
private const val DIALOG_UNLINK_TREE = "unlinkTrees"
private const val DIALOG_COLOR = "colorNotes"

class NoteListActivity :
    AppCompatActivity(),
    AlertDialog.DialogListener,
    SingleChoiceDialog.DialogListener,
    SwipeRefreshLayout.OnRefreshListener {

    // data
    private lateinit var model: NoteListViewModel

    // view
    private lateinit var binding: ActivityNotelistBinding
    private lateinit var adapter: NoteListAdapter
    private lateinit var fabMenu: FabMenu

    // settings helper
    private var notePreviewColor = prefs.notePreviewColor
    private var notePreviewMetadata = prefs.notePreviewMetadata
    private var notePreviewMime = prefs.notePreviewMime
    private var notePreviewMaxLines = prefs.notePreviewMaxLines

    // saf result launcher
    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        Log.i(LOG_TAG, "open document uri result: $uri")

        model.handleOpenedDocument(uri)
    }
    private val openDocumentTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@registerForActivityResult
        Log.i(LOG_TAG, "open document tree uri result: $uri")

        model.handleOpenedTree(uri)
    }

    // lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        updateDayNight()
        super.onCreate(savedInstanceState)
        binding = ActivityNotelistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initFabMenu()
        initRecycler()
        initRefreshLayout()
        initData()
    }

    override fun onRestart() {
        super.onRestart()
        if (fabMenu.isMenuOpen) fabMenu.closeMenu()

        // apply updated settings
        if (
            notePreviewColor != prefs.notePreviewColor
            || notePreviewMetadata != prefs.notePreviewMetadata
            || notePreviewMime != prefs.notePreviewMime
            || notePreviewMaxLines != prefs.notePreviewMaxLines
        ) {
            adapter.notifyDataSetChanged()
            notePreviewColor = prefs.notePreviewColor
            notePreviewMetadata = prefs.notePreviewMetadata
            notePreviewMime = prefs.notePreviewMime
            notePreviewMaxLines = prefs.notePreviewMaxLines
        }
    }

    override fun onStart() {
        super.onStart()
        model.loadNotes()
        onRefresh()
    }

    override fun onBackPressed() = when {
        fabMenu.isMenuOpen -> fabMenu.closeMenu()
        model.isSelectionMode -> emptySelection()
        else -> super.onBackPressed()
    }

    // menu

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
            findItem(R.id.action_sort_reverse)?.isChecked = prefs.reverseOrder

            // set gridView
            findItem(R.id.action_view_toggle)?.let {
                it.isChecked = prefs.gridView
                if (prefs.gridView) it.setIcon(R.drawable.ic_view_list_24).setTitle(R.string.action_view_list)
                else it.setIcon(R.drawable.ic_view_grid_24).setTitle(R.string.action_view_grid)
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (model.selectionCount) {
            0 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_notelist_tb, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)

                // change scroll flags: scroll and snap tb
                enableToolbarScroll()
                //binding.ab.setExpanded(false, true)

                // search
                val searchItem = menu.findItem(R.id.action_search)
                searchItem.searchView.apply {
                    // set hint (xml attribute doesn't work)
                    queryHint = getString(R.string.tv_notelist_search_hint)

                    // set background
                    setBackgroundResource(R.drawable.layer_searchview_bg)

                    // set close icon (the default is not of 'round' style)
                    closeIcon.setImageResource(R.drawable.ic_close_24)
                    closeIcon.enableActionItemRipple()

                    // set listener
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            // clear focus to not reopen keyboard when activity is resumed
                            clearFocus()
                            return true
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            model.searchNotes(newText)
                            return true
                        }
                    })
                }

                // swiperefresh
                binding.srl.isEnabled = true
            }
            1 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_notelist_tb_selection_single, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                // change scroll flags: don't scroll tb
                disableToolbarScroll()
                binding.ab.setExpanded(true, false) // anim is not smooth

                // pin action
                updatePinMenuItem(menu)

                // swiperefresh
                binding.srl.isEnabled = false
            }
            else -> {
                // set menu
                menuInflater.inflate(R.menu.menu_notelist_tb_selection_multi, menu)

                // pin action
                updatePinMenuItem(menu)

                // swiperefresh
                binding.srl.isEnabled = false
            }
        }

        menu.setOptionalIconsVisible(true)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // normal tb
            R.id.action_settings -> launchActivity<SettingsActivity>()
            R.id.action_view_toggle -> {
                adapter.invertViewType()
                setAdapterAndManager()
                invalidateOptionsMenu() // update action icon
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
            R.id.action_sort_reverse -> {
                item.isChecked = !item.isChecked
                prefs.reverseOrder = item.isChecked
                model.loadNotes()
            }

            // single selection tb
            //R.id.action_copy -> copySelection()

            // multi selection tb
            android.R.id.home -> emptySelection()
            R.id.action_pin -> pinSelection()
            R.id.action_color -> colorSelection()
            R.id.action_select_all -> selectAll()
            R.id.action_unlink -> unlinkSelection()
            R.id.action_unlink_tree -> unlinkSelectionTrees()
            R.id.action_delete -> deleteSelection()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun onOptionsRadioItemSelected(item: MenuItem) {
        item.isChecked = true
        model.loadNotes()
        // rebind to update modified/opened tv
        adapter.notifyDataSetChanged()
    }

    // swiperefresh

    private fun initRefreshLayout() {
        binding.srl.setOnRefreshListener(this)
        binding.srl.setProgressBackgroundColorSchemeColor(getColorByAttr(R.attr.colorSurface))
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
                if (model.isSelectionMode) selectNote(it)
                else NoteDetailActivity.startActivity(this, it.uri, it.colorIndex, model.searchQueryOrNull)
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
        val spanCount =
            if (!prefs.gridView) getInteger(R.integer.quantity_notelist_list_columns)
            else if (isPortrait) getInteger(R.integer.quantity_notelist_grid_columns_portrait)
            else getInteger(R.integer.quantity_notelist_grid_columns_landscape)

        val manager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        binding.rv.layoutManager = manager
    }

    private fun submitItems(items: List<Note>) {
        // when searching, keeping scroll state results in irregularities
        if (model.isSearching) adapter.submitList(items)
        else adapter.submitListKeepScroll(items, binding.rv.layoutManager)

        // toggle empty page
        if (items.isEmpty()) {
            binding.inEmpty.root.isGone = false

            // set search empty page
            if (model.isSearching) {
                binding.inEmpty.tvEmptyTitle.text = getString(R.string.tv_notelist_empty_search_title)
                binding.inEmpty.tvEmptyMessage.text = getString(R.string.tv_notelist_empty_search_message)
                binding.inEmpty.ivEmpty.setImageDrawable(
                    getDrawableCompatWithTint(R.drawable.ic_search_24, R.attr.colorAccent)
                )
            }
            // set new user empty page
            else {
                binding.inEmpty.tvEmptyTitle.text = getString(R.string.tv_notelist_empty_new_title)
                binding.inEmpty.tvEmptyMessage.text = getString(R.string.tv_notelist_empty_new_message)
                binding.inEmpty.ivEmpty.setImageDrawable(
                    getDrawableCompatWithTint(R.drawable.ic_note_24, R.attr.colorAccent)
                )
            }
        }
        else binding.inEmpty.root.isGone = true
    }

    // fab

    private fun initFabMenu() {
        fabMenu = FabMenu(this, layoutInflater, binding.root).apply {
            // create note
            addItem(
                getString(R.string.tv_notelist_fab_create),
                getDrawableCompat(R.drawable.ic_create_24)
            ) {
                emptySelection()
                NoteDetailActivity.startActivity(this@NoteListActivity)
            }

            // link note
            addItem(
                getString(R.string.tv_notelist_fab_link),
                getDrawableCompat(R.drawable.ic_link_24)
            ) {
                emptySelection()
                model.linkNote(openDocument)
            }

            // link folder
            addItem(
                getString(R.string.tv_notelist_fab_link_folder),
                getDrawableCompat(R.drawable.ic_folder_add_24)
            ) {
                emptySelection()
                model.linkFolder(openDocumentTree)
            }
        }
    }

    // toolbar

    private fun initToolbar() {
        setSupportActionBar(binding.tb)
        supportActionBar?.setHomeAsUpIndicator(
            getDrawableCompatWithTint(R.drawable.ic_close_24, R.attr.colorControlNormal)
        )
    }

    private fun updateToolbarTitle() {
        if (!model.isSelectionMode) {
            supportActionBar?.title = getString(R.string.title_activity_notelist)
        }
        else {
            supportActionBar?.title =
                getQuantityString(R.plurals.tb_notelist_title_selection, model.selectionCount)
        }
    }

    private fun enableToolbarScroll() {
        val params: AppBarLayout.LayoutParams = binding.ctb.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        binding.ctb.layoutParams = params
    }

    private fun disableToolbarScroll() {
        val params: AppBarLayout.LayoutParams = binding.ctb.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        binding.ctb.layoutParams = params
    }

    private fun updatePinMenuItem(menu: Menu) {
        val unpinSelection = model.isSelectionPinned
        val pinItem = menu.findItem(R.id.action_pin)

        if (unpinSelection) {
            pinItem.setIcon(R.drawable.ic_pin_24)
            pinItem.tooltipText = getString(R.string.action_unpin)
        }
        else {
            pinItem.setIcon(R.drawable.ic_pin_outline_24)
            pinItem.tooltipText = getString(R.string.action_pin)
        }
    }

    // data

    private fun initData() {
        val container = (application as App).appContainer
        model = container.noteListViewModel

        model.itemsData.observe(this) { items ->
            submitItems(items)
        }
    }

    private fun copySelection() {
        // TODO: needs createAndSave
        /*selectedNote?.let {
            val copy = it.copy(id = NO_ID)
            db.noteDao().addNote(copy)
            emptySelection()
        }*/
    }

    private fun pinSelection() {
        model.pinSelectedNotes()
        emptySelection()
    }

    private fun colorSelection() = colorDialog(
        title = getString(R.string.dialog_title_color_notes),
        colors = getIntegerArray(R.array.note_palette),
        checkedIndex = model.selectedNotes.common { it.colorIndex },
        tag = DIALOG_COLOR
    ).show(supportFragmentManager)

    private fun unlinkSelection() = alertDialog(
        title = getQuantityString(R.plurals.dialog_title_unlink_notes, model.selectionCount),
        message = getQuantityString(R.plurals.dialog_msg_unlink_notes, model.selectionCount),
        posBtnTxtRes = R.string.dialog_btn_unlink,
        tag = DIALOG_UNLINK
    ).show(supportFragmentManager)

    private fun unlinkSelectionTrees() = alertDialog(
        title = getQuantityString(R.plurals.dialog_title_unlink_tree, model.treeSelectionCount),
        message = getQuantityString(R.plurals.dialog_msg_unlink_trees, model.treeSelectionCount),
        posBtnTxtRes = R.string.dialog_btn_unlink,
        tag = DIALOG_UNLINK_TREE
    ).show(supportFragmentManager)

    private fun deleteSelection() = alertDialog(
        title = getQuantityString(R.plurals.dialog_title_delete_notes, model.selectionCount),
        message = getString(R.string.dialog_msg_delete_notes),
        posBtnTxtRes = R.string.dialog_btn_delete,
        tag = DIALOG_DELETE
    ).show(supportFragmentManager)

    // selection

    private fun selectNote(note: Note) {
        // sync with data and adapter
        val index = model.toggleNoteSelection(note)
        adapter.notifyItemChanged(index)

        // sync tb
        invalidateOptionsMenu()
        updateToolbarTitle()
    }

    private fun selectAll() {
        for (note in model.items) {
            if (note.isSelected) continue

            // sync with data and adapter
            val index = model.toggleNoteSelection(note)
            adapter.notifyItemChanged(index)
        }

        // sync tb
        invalidateOptionsMenu()
        updateToolbarTitle()
    }

    private fun emptySelection() {
        for (note in model.selectedNotes) {
            note.isSelected = false

            val index = model.items.indexOf(note)
            model.itemsData.value?.set(index, note)
            adapter.notifyItemChanged(index)
        }
        model.selectionIndices.removeAll()

        // sync tb
        invalidateOptionsMenu()
        updateToolbarTitle()
    }

    // dialog

    override fun onAlertDialogPositiveClick(passValue: String?, tag: String) {
        when (tag) {
            DIALOG_DELETE -> {
                model.deleteSelectedNotes()
                emptySelection()
            }
            DIALOG_UNLINK -> {
                model.unlinkSelectedNotes()
                emptySelection()
            }
            DIALOG_UNLINK_TREE -> {
                model.unlinkSelectedTrees()
                emptySelection()
            }
        }
    }

    override fun onSingleChoiceDialogItemSelected(selectedIndex: Int, tag: String) {
        when (tag) {
            DIALOG_COLOR -> {
                model.colorSelectedNotes(selectedIndex)
                emptySelection()
            }
        }
    }
}