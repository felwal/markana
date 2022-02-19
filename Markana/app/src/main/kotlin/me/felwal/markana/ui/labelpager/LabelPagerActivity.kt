package me.felwal.markana.ui.labelpager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.children
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import me.felwal.android.util.closeIcon
import me.felwal.android.util.common
import me.felwal.android.util.getDrawableCompatWithTint
import me.felwal.android.util.getIntegerArray
import me.felwal.android.util.getQuantityString
import me.felwal.android.util.launchActivity
import me.felwal.android.util.popup
import me.felwal.android.util.searchView
import me.felwal.android.util.setActionItemRipple
import me.felwal.android.util.setBorderlessItemRipple
import me.felwal.android.util.setOptionalIconsVisible
import me.felwal.android.widget.control.DialogOption
import me.felwal.android.widget.control.InputOption
import me.felwal.android.widget.control.RadioGroupOption
import me.felwal.android.widget.dialog.AlertDialog
import me.felwal.android.widget.dialog.InputDialog
import me.felwal.android.widget.dialog.SingleChoiceDialog
import me.felwal.android.widget.dialog.alertDialog
import me.felwal.android.widget.dialog.colorDialog
import me.felwal.android.widget.dialog.inputDialog
import me.felwal.android.widget.dialog.radioDialog
import me.felwal.markana.R
import me.felwal.markana.app
import me.felwal.markana.data.Label
import me.felwal.markana.data.prefs.SortBy
import me.felwal.markana.databinding.ActivityLabelpagerBinding
import me.felwal.markana.prefs
import me.felwal.markana.ui.notedetail.NoteDetailActivity
import me.felwal.markana.ui.setting.SettingsActivity
import me.felwal.markana.util.i
import me.felwal.markana.util.updateDayNight

private const val DIALOG_DELETE_NOTES = "deleteNotes"
private const val DIALOG_UNLINK_NOTES = "unlinkNotes"
private const val DIALOG_UNLINK_TREES = "unlinkTrees"
private const val DIALOG_COLOR_NOTES = "colorNotes"
private const val DIALOG_LABEL_NOTES = "labelNotes"
private const val DIALOG_ADD_LABEL = "addLabel"
private const val DIALOG_RENAME_LABEL = "renameLabel"
private const val DIALOG_DELETE_LABEL = "deleteLabel"

class LabelPagerActivity :
    AppCompatActivity(),
    AlertDialog.DialogListener,
    SingleChoiceDialog.DialogListener,
    InputDialog.DialogListener {

    // data
    private lateinit var model: LabelPagerViewModel

    // view
    lateinit var binding: ActivityLabelpagerBinding
    private var stateAdapter: LabelPagerStateAdapter? = null

    // settings helper
    // maybe there is a better way?
    private var notePreviewColor = prefs.notePreviewColor
    private var notePreviewListIcon = prefs.notePreviewListIcon
    private var notePreviewMetadata = prefs.notePreviewMetadata
    private var notePreviewMime = prefs.notePreviewMime
    private var notePreviewMaxLines = prefs.notePreviewMaxLines

    // saf result launcher
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        i("open document uri result: $uri")

        model.handleOpenedDocument(uri)
    }
    private val openDocumentTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri ?: return@registerForActivityResult
            i("open document tree uri result: $uri")

            model.handleOpenedTree(uri)
        }

    // lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        updateDayNight()
        super.onCreate(savedInstanceState)
        binding = ActivityLabelpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fam.onSetContentView()

        initToolbar()
        initFam()
        initData()
    }

    override fun onPause() {
        prefs.selectedLabelPosition = binding.tl.selectedTabPosition
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
        if (binding.fam.isMenuOpen) binding.fam.closeMenu()

        // apply updated settings
        if (
            notePreviewColor != prefs.notePreviewColor
            || notePreviewListIcon != prefs.notePreviewListIcon
            || notePreviewMetadata != prefs.notePreviewMetadata
            || notePreviewMime != prefs.notePreviewMime
            || notePreviewMaxLines != prefs.notePreviewMaxLines
        ) {
            model.notifyAdapters()

            notePreviewColor = prefs.notePreviewColor
            notePreviewListIcon = prefs.notePreviewListIcon
            notePreviewMetadata = prefs.notePreviewMetadata
            notePreviewMime = prefs.notePreviewMime
            notePreviewMaxLines = prefs.notePreviewMaxLines
        }
    }

    override fun onBackPressed() = when {
        binding.fam.isMenuOpen -> binding.fam.closeMenu()
        model.isSelectionMode -> deselectAll()
        else -> super.onBackPressed()
    }

    // menu

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            // set sort by
            findItem(
                when (prefs.sortBy) {
                    SortBy.NAME -> R.id.action_sort_name
                    SortBy.MODIFIED -> R.id.action_sort_modified
                    SortBy.OPENED -> R.id.action_sort_opened
                }
            )?.isChecked = true

            // set sort options
            findItem(R.id.action_sort_reverse)?.isChecked = prefs.reverseOrder

            // set filter
            findItem(R.id.action_filter_show_archived)?.isChecked = prefs.showArchived

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
                menuInflater.inflate(R.menu.menu_labelpager_tb, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)

                // change scroll flags: scroll and snap tb
                enableToolbarScroll()
                //binding.ab.setExpanded(false, true)

                // search
                val searchItem = menu.findItem(R.id.action_search)
                searchItem.searchView.apply {
                    // set hint (xml attribute doesn't work)
                    queryHint = getString(R.string.tv_labelpager_search_hint)

                    // set background
                    setBackgroundResource(R.drawable.layer_searchview_bg)

                    // set close icon (the default is not of 'round' style)
                    closeIcon.setImageResource(R.drawable.ic_close_24)
                    closeIcon.setActionItemRipple()

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
                model.setRefreshLayoutsEnabled(true)
            }
            1 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_labelpager_tb_selection_single, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                // change scroll flags: don't scroll tb
                disableToolbarScroll()
                binding.ab.setExpanded(true, false) // anim is not smooth

                // update selection-count dependent actions
                updatePinMenuItem(menu)
                updateArchiveMenuItem(menu)

                // swiperefresh
                model.setRefreshLayoutsEnabled(false)
            }
            else -> {
                // set menu
                menuInflater.inflate(R.menu.menu_labelpager_tb_selection_multi, menu)

                // update selection-count dependent actions
                updatePinMenuItem(menu)
                updateArchiveMenuItem(menu)

                // swiperefresh
                model.setRefreshLayoutsEnabled(false)
            }
        }

        menu.setOptionalIconsVisible(true)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // normal tb
            R.id.action_settings -> launchActivity<SettingsActivity>()
            R.id.action_view_toggle -> invertViewType()

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
            R.id.action_filter_show_archived -> {
                item.isChecked = !item.isChecked
                prefs.showArchived = item.isChecked
                model.loadNotes()
            }

            // single selection tb
            //R.id.action_copy -> copySelection()

            // single/multi selection tb
            android.R.id.home -> deselectAll()
            R.id.action_pin -> pinSelection()
            R.id.action_color -> colorSelection()
            R.id.action_select_all -> selectAll()
            R.id.action_label -> labelSelection()
            R.id.action_archive -> archiveSelection()
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
        model.notifyAdapters()
    }

    // data

    private fun initData() {
        val container = app.appContainer
        model = container.labelPagerViewModel

        model.loadLables()
        model.labelsData.observe(this) { labels ->
            container.createNoteListViewModels(labels)
            initViewPager(labels)
        }
    }

    private fun invertViewType() {
        prefs.gridView = !prefs.gridView

        model.notifyAdapters()
        model.notifyManagers()

        // update action icon
        invalidateOptionsMenu()
    }

    // pager

    private fun initViewPager(labels: List<Label>) {
        stateAdapter = LabelPagerStateAdapter(labels.size, this)
        binding.vp.adapter = stateAdapter

        // link the TabLayout to the ViewPager
        TabLayoutMediator(binding.tl, binding.vp) { tab, position ->
            tab.text = labels[position].name
        }.attach()

        // load selected tab
        binding.tl.apply {
            selectTab(getTabAt(prefs.selectedLabelPosition))
        }

        // popup menu
        for ((i, tab) in binding.tl.tabs.withIndex()) {
            tab.setBorderlessItemRipple()

            tab.setOnLongClickListener { v ->
                popup(v, R.menu.menu_labelpager_popup_label) { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_rename_label -> renameLabel(labels[i])
                        R.id.action_delete_label -> deleteLabel(labels[i])
                    }
                    true
                }
                true
            }
        }

        //
        model.setRefreshLayoutsEnabled(!model.isSelectionMode)
    }

    val TabLayout.tabs get() = (getChildAt(0) as ViewGroup).children

    // fab & swiperefresh

    private fun initFam() {
        binding.fam.apply {
            // create note
            addItem(
                getString(R.string.tv_labelpager_fab_create),
                R.drawable.ic_create_24
            ) {
                deselectAll()
                NoteDetailActivity.startActivity(this@LabelPagerActivity)
            }

            // link note
            addItem(
                getString(R.string.tv_labelpager_fab_link),
                R.drawable.ic_link_24
            ) {
                deselectAll()
                model.linkNote(openDocumentLauncher)
            }

            // link folder
            addItem(
                getString(R.string.tv_labelpager_fab_link_folder),
                R.drawable.ic_folder_add_24
            ) {
                deselectAll()
                model.linkFolder(openDocumentTreeLauncher)
            }

            // add label
            addItem(
                getString(R.string.tv_labelpager_fab_add_label),
                R.drawable.ic_label_24
            ) {
                closeMenu()
                inputDialog(
                    DialogOption(getString(R.string.dialog_title_add_label), "", tag = DIALOG_ADD_LABEL),
                    InputOption(hint = getString(R.string.dialog_et_hint_add_label))
                ).show(supportFragmentManager)
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
            supportActionBar?.title = getString(R.string.title_activity_labelpager)
        }
        else {
            supportActionBar?.title =
                getQuantityString(R.plurals.tb_labelpager_title_selection, model.selectionCount)
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
        // only unpin if all are pinned
        val unpinSelection = model.isSelectionPinned
        val pinItem = menu.findItem(R.id.action_pin)

        if (unpinSelection) {
            pinItem.setIcon(R.drawable.ic_pin_24)
            pinItem.tooltipText = getString(R.string.action_unpin)
            pinItem.title = getString(R.string.action_unpin)
        }
        else {
            pinItem.setIcon(R.drawable.ic_pin_outline_24)
            pinItem.tooltipText = getString(R.string.action_pin)
            pinItem.title = getString(R.string.action_pin)
        }
    }

    private fun updateArchiveMenuItem(menu: Menu) {
        // only unarchive if all are archived
        val unarchiveSelection = model.isSelectionArchived
        val archiveItem = menu.findItem(R.id.action_archive)

        if (unarchiveSelection) {
            archiveItem.setIcon(R.drawable.ic_unarchive_24)
            archiveItem.tooltipText = getString(R.string.action_unarchive)
            archiveItem.title = getString(R.string.action_unarchive)
        }
        else {
            archiveItem.setIcon(R.drawable.ic_archive_24)
            archiveItem.tooltipText = getString(R.string.action_archive)
            archiveItem.title = getString(R.string.action_archive)
        }
    }

    fun syncToolbar() {
        invalidateOptionsMenu()
        updateToolbarTitle()
    }

    //

    private fun renameLabel(label: Label) = inputDialog(
        DialogOption(
            getString(R.string.dialog_title_rename_label),
            tag = DIALOG_RENAME_LABEL,
            passValue = label.id.toString()
        ),
        InputOption(
            text = label.name,
            hint = getString(R.string.dialog_et_hint_add_label)
        )
    ).show(supportFragmentManager)

    private fun deleteLabel(label: Label) = alertDialog(
        DialogOption(
            title = getString(R.string.dialog_title_delete_label),
            message = getString(R.string.dialog_msg_delete_label),
            tag = DIALOG_DELETE_LABEL,
            passValue = label.id.toString()
        )
    ).show(supportFragmentManager)

    // selection

    private fun selectAll() {
        model.selectAllNotes(binding.tl.selectedTabPosition)
        syncToolbar()
    }

    private fun deselectAll() {
        model.deselectAllNotes()
        syncToolbar()
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
        deselectAll()
    }

    private fun colorSelection() = colorDialog(
        DialogOption(getString(R.string.dialog_title_color_notes), tag = DIALOG_COLOR_NOTES),
        colors = getIntegerArray(R.array.note_palette),
        checkedIndex = model.selectedNotes.common { it.colorIndex }
    ).show(supportFragmentManager)

    private fun labelSelection() {
        val names = model.labels.map { it.name }
        val selectedIndex = model.labels.map { it.id }.indexOf(model.selectedNotes[0].labelId)
        val icons = IntArray(names.size) { R.drawable.ic_label_24 }

        radioDialog(
            DialogOption(getString(R.string.dialog_title_label_notes), tag = DIALOG_LABEL_NOTES),
            RadioGroupOption(names.toTypedArray(), selectedIndex, icons)
        ).show(supportFragmentManager)
    }

    private fun archiveSelection() {
        model.archiveSelectedNotes()
        deselectAll()
    }

    private fun unlinkSelection() = alertDialog(
        DialogOption(
            title = getQuantityString(R.plurals.dialog_title_unlink_notes, model.selectionCount),
            message = getQuantityString(R.plurals.dialog_msg_unlink_notes, model.selectionCount),
            posBtnTxtRes = R.string.dialog_btn_unlink,
            tag = DIALOG_UNLINK_NOTES
        )
    ).show(supportFragmentManager)

    private fun unlinkSelectionTrees() = alertDialog(
        DialogOption(
            title = getQuantityString(R.plurals.dialog_title_unlink_tree, model.treeSelectionCount),
            message = getQuantityString(R.plurals.dialog_msg_unlink_trees, model.treeSelectionCount),
            posBtnTxtRes = R.string.dialog_btn_unlink,
            tag = DIALOG_UNLINK_TREES
        )
    ).show(supportFragmentManager)

    private fun deleteSelection() = alertDialog(
        DialogOption(
            title = getQuantityString(R.plurals.dialog_title_delete_notes, model.selectionCount),
            message = getString(R.string.dialog_msg_delete_notes),
            posBtnTxtRes = R.string.dialog_btn_delete,
            tag = DIALOG_DELETE_NOTES
        )
    ).show(supportFragmentManager)

    // dialog

    override fun onAlertDialogPositiveClick(tag: String, passValue: String?) {
        when (tag) {
            DIALOG_DELETE_NOTES -> {
                model.deleteSelectedNotes()
                deselectAll()
            }
            DIALOG_UNLINK_NOTES -> {
                model.unlinkSelectedNotes()
                deselectAll()
            }
            DIALOG_UNLINK_TREES -> {
                model.unlinkSelectedTrees()
                deselectAll()
            }
            DIALOG_DELETE_LABEL -> {
                passValue?.let { model.deleteLabel(it.toLong()) }
            }
        }
    }

    override fun onSingleChoiceDialogItemSelected(selectedIndex: Int, tag: String, passValue: String?) {
        when (tag) {
            DIALOG_COLOR_NOTES -> {
                model.colorSelectedNotes(selectedIndex)
                deselectAll()
            }
            DIALOG_LABEL_NOTES -> {
                model.labelSelectedNotes(selectedIndex)
                deselectAll()
            }
        }
    }

    override fun onInputDialogPositiveClick(input: String, tag: String, passValue: String?) {
        when (tag) {
            DIALOG_ADD_LABEL -> model.addLabel(input)
            DIALOG_RENAME_LABEL -> {
                passValue?.let { model.renameLabel(it.toLong(), input) }
            }
        }
    }
}