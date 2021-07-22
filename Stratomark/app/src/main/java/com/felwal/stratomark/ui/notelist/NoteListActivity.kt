package com.felwal.stratomark.ui.notelist

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.felwal.stratomark.MainApplication
import com.felwal.stratomark.R
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.databinding.ActivityNoteListBinding
import com.felwal.stratomark.ui.notedetail.NoteDetailActivity
import com.felwal.stratomark.ui.setting.SettingsActivity
import com.felwal.stratomark.util.animateFab
import com.felwal.stratomark.util.crossfadeIn
import com.felwal.stratomark.util.crossfadeOut
import com.felwal.stratomark.util.defaults
import com.felwal.stratomark.util.empty
import com.felwal.stratomark.util.getAttrColor
import com.felwal.stratomark.util.launchActivity
import com.felwal.stratomark.util.toggleInclusion
import com.felwal.stratomark.util.visibleOrGone
import com.google.android.material.appbar.AppBarLayout

private const val LOG_TAG = "NoteList"
const val OVERLAY_ALPHA = 0.96f

class NoteListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteListBinding
    private lateinit var adapter: NoteAdapter
    private lateinit var model: NoteListViewModel

    private val selectionCount: Int get() = model.selectionIndices.size
    private val selectionMode: Boolean get() = selectionCount != 0
    private val selectedNote: Note? get() = if (selectionCount > 0) model.selectedNotes[0] else null

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        Log.i(LOG_TAG, "open document uri result: $uri")

        model.handleCreatedNote(uri)
    }

    private var isFabMenuOpen: Boolean = false

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

        // animate tb and fab on scroll
        binding.rv.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // animate tb
            binding.ab.isSelected = binding.rv.canScrollVertically(-1)

            // show/hide fab
            val dy = scrollY - oldScrollY
            if (binding.fab.isOrWillBeShown && dy > 0) binding.fab.hide()
            else if (binding.fab.isOrWillBeHidden && dy < 0) binding.fab.show()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (selectionCount) {
            0 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_tb_note_list, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)

                // change scroll flags: scroll and snap tb
                enableToolbarScroll()
                //binding.ab.setExpanded(false, true)
            }
            1 -> {
                // set menu
                menuInflater.inflate(R.menu.menu_tb_note_list_selection_single, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                // change scroll flags: don't scroll tb
                disableToolbarScroll()
                binding.ab.setExpanded(true, false) // anim is not smooth
            }
            else -> {
                menuInflater.inflate(R.menu.menu_tb_note_list_selection_multi, menu)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = true defaults when (item.itemId) {
        // normal tb
        R.id.action_settings -> launchActivity<SettingsActivity>()
        +
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
        if (isFabMenuOpen) closeFabMenu()
    }


    override fun onBackPressed() = when {
        isFabMenuOpen -> closeFabMenu()
        selectionMode -> emptySelection()
        else -> super.onBackPressed()
    }

    // recycler

    private fun initRecycler() {
        adapter = NoteAdapter(
            onClick = {
                if (selectionMode) selectNote(it)
                else NoteDetailActivity.startActivity(this, it.uri)
            },
            onLongClick = {
                selectNote(it)
            }
        )
        binding.rv.adapter = adapter
        binding.rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun submitItems(items: List<Note>) {
        adapter.submitList(items)

        // toggle empty page
        binding.clEmpty.visibility = visibleOrGone(items.isEmpty())
    }

    // fab

    private fun initFabMenu() {
        // open/close fab menu
        binding.fab.setOnClickListener {
            if (isFabMenuOpen) closeFabMenu() else openFabMenu()
        }

        // create note; launch NoteDetailActivity
        binding.fabCreate.setOnClickListener {
            emptySelection()
            NoteDetailActivity.startActivity(this)
        }

        // link note; launch Storage Access Framework
        binding.fabLink.setOnClickListener {
            emptySelection()
            model.linkNote(openDocument)
        }

        // close fab menu
        binding.vOverlay.setOnClickListener { closeFabMenu() }
    }

    private fun openFabMenu() {
        animateFab()
        binding.fabCreate.show()
        binding.fabLink.show()

        binding.clFabsCreate.crossfadeIn()
        binding.clFabsLink.crossfadeIn()
        binding.vOverlay.crossfadeIn(OVERLAY_ALPHA)

        isFabMenuOpen = true
    }

    private fun closeFabMenu() {
        animateFab()
        binding.fabLink.hide()
        binding.fabCreate.hide()

        binding.clFabsLink.crossfadeOut()
        binding.clFabsCreate.crossfadeOut()
        binding.vOverlay.crossfadeOut()

        isFabMenuOpen = false
    }

    private fun animateFab() {
        @ColorInt val closedColor: Int = getAttrColor(R.attr.colorSecondary)
        @ColorInt val openColor: Int = getAttrColor(R.attr.colorSurface)

        @ColorInt val fromColor: Int
        @ColorInt val toColor: Int
        val toIcon: Drawable?

        // animate to closed menu
        if (isFabMenuOpen) {
            fromColor = openColor
            toColor = closedColor
            toIcon = ContextCompat.getDrawable(this, R.drawable.ic_add)?.mutate()
            toIcon?.setColorFilter(getAttrColor(R.attr.colorOnSecondary), PorterDuff.Mode.SRC_IN)
        }
        // animate to open menu
        else {
            fromColor = closedColor
            toColor = openColor
            toIcon = ContextCompat.getDrawable(this, R.drawable.ic_clear)?.mutate()
            toIcon?.setColorFilter(getAttrColor(R.attr.colorControlActivated), PorterDuff.Mode.SRC_IN)
        }

        binding.fab.animateFab(fromColor, toColor, toIcon)
    }

    // toolbar

    private fun updateToolbarTitle() {
        if (selectionCount == 0) {
            supportActionBar?.title = getString(R.string.title_activity_note_list)
        }
        else {
            supportActionBar?.title =
                resources.getQuantityString(R.plurals.tb_title_note_list_selection, selectionCount, selectionCount)
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

    private fun deleteSelection() = deleteNotes(model.selectedNotes)

    private fun unlinkSelection() = unlinkNotes(model.selectedNotes)

    // selection

    private fun selectNote(note: Note) {
        note.selected = !note.selected

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
            note.selected = false

            val index = model.items.indexOf(note)
            model.itemsData.value?.set(index, note)
            adapter.notifyItemChanged(index)
        }
        model.selectionIndices.empty()

        // sync tb
        invalidateOptionsMenu()
        updateToolbarTitle()
    }
}