package com.felwal.stratomark.ui.notelist

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.felwal.stratomark.R
import com.felwal.stratomark.data.AppDatabase
import com.felwal.stratomark.data.NO_ID
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.databinding.ActivityNoteListBinding
import com.felwal.stratomark.ui.notedetail.NoteDetailActivity
import com.felwal.stratomark.ui.setting.SettingsActivity
import com.felwal.stratomark.util.empty
import com.felwal.stratomark.util.getAttrColor
import com.felwal.stratomark.util.launchActivity
import com.felwal.stratomark.util.toggleInclusion
import com.google.android.material.appbar.AppBarLayout
import kotlin.concurrent.thread

class NoteListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteListBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: NoteAdapter

    private val selectedItems: MutableList<Note> = mutableListOf()
    private var _items: MutableList<Note> = mutableListOf()

    private val items: MutableList<Note>
        get() {
            val freshItems = db.noteDao().getAllNotes().toMutableList()
            if (freshItems != _items) _items = freshItems
            return _items
        }

    private val selectionCount: Int get() = selectedItems.size

    private val selectionMode: Boolean get() = selectionCount != 0

    private val selectedItem: Note? get() = if (selectionCount > 0) selectedItems[0] else null

    // Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tb)

        // tb: deselect as home
        val homeIcon = ContextCompat.getDrawable(this, R.drawable.ic_cancel)!!.mutate()
        homeIcon.setColorFilter(getAttrColor(R.attr.colorControlActivated), PorterDuff.Mode.SRC_IN)
        supportActionBar?.setHomeAsUpIndicator(homeIcon)

        // db
        db = AppDatabase.getInstance(applicationContext)
        db.onWriteListener = { submitItems() }

        // animate tb elevation on scroll
        binding.rv.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.rv.canScrollVertically(-1)
        }

        initFab()
        initRecycler()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (selectionCount) {
            0 -> {
                menuInflater.inflate(R.menu.menu_tb_note_list, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)

                // change scroll flags: scroll and snap tb
                val params: AppBarLayout.LayoutParams = binding.ctb.layoutParams as AppBarLayout.LayoutParams
                params.scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
                binding.ctb.layoutParams = params

                //binding.ab.setExpanded(false, true)
            }
            1 -> {
                menuInflater.inflate(R.menu.menu_tb_note_list_selection_single, menu)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                // change scroll flags: don't scroll tb
                val params: AppBarLayout.LayoutParams = binding.ctb.layoutParams as AppBarLayout.LayoutParams
                params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
                binding.ctb.layoutParams = params

                binding.ab.setExpanded(true, false) // anim is not smooth
            }
            else -> {
                menuInflater.inflate(R.menu.menu_tb_note_list_selection_multi, menu)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            // normal tb
            R.id.action_settings -> launchActivity<SettingsActivity>()

            // single selection tb
            R.id.action_copy -> copyNote()

            // multi selection tb
            android.R.id.home -> {
                emptySelection()
                true
            }
            R.id.action_delete -> deleteNote()

            else -> super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() {
        if (selectionMode) emptySelection()
        else super.onBackPressed()
    }

    // view

    private fun initFab() = binding.fab.setOnClickListener {
        emptySelection()
        NoteDetailActivity.startActivity(this)
    }

    private fun initRecycler() {
        adapter = NoteAdapter(
            onClick = {
                if (selectionMode) selectNote(it)
                else NoteDetailActivity.startActivity(this, it.noteId)
            },
            onLongClick = {
                selectNote(it)
            }
        )
        binding.rv.adapter = adapter
        binding.rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        submitItems()
    }

    private fun updateToolbarTitle() {
        if (selectionCount == 0) {
            supportActionBar?.title = getString(R.string.title_activity_note_list)
        }
        else {
            supportActionBar?.title =
                resources.getQuantityString(R.plurals.tb_title_note_list_selection, selectionCount, selectionCount)
        }
    }

    //

    private fun submitItems() = thread {
        val notes = items

        runOnUiThread {
            adapter.submitList(notes)

            // toggle empty page
            binding.clEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    // note actions

    private fun copyNote(): Boolean {
        selectedItem?.let {
            val copy = it.copy(noteId = NO_ID)
            thread {
                db.noteDao().addNote(copy)
                db.invokeWriteListener(this)
                runOnUiThread { emptySelection() }
            }
        }
        return true
    }

    private fun deleteNote(): Boolean {
        thread {
            db.noteDao().deleteNotes(selectedItems)
            db.invokeWriteListener(this)
            runOnUiThread { emptySelection(/*false*/) }
        }
        return true
    }

    // selection

    private fun selectNote(note: Note) {
        note.selected = !note.selected

        // sync with lists and adapter
        val index = _items.indexOf(note)
        _items[index] = note
        selectedItems.toggleInclusion(note)
        adapter.notifyItemChanged(index)

        // selection mode just turned off/single/multi; sync tb
        if (selectionCount in 0..2) invalidateOptionsMenu()
        updateToolbarTitle()
    }

    private fun emptySelection(notifyAdapter: Boolean = true) {
        for (note in selectedItems) {
            note.selected = false

            if (notifyAdapter) {
                val index = _items.indexOf(note)
                adapter.notifyItemChanged(index)
            }
        }
        selectedItems.empty()

        // sync tb
        invalidateOptionsMenu()
        updateToolbarTitle()
    }
}