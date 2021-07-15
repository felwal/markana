package com.felwal.stratomark.ui.notelist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import com.felwal.stratomark.R
import com.felwal.stratomark.data.AppDatabase
import com.felwal.stratomark.data.NO_ID
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.databinding.ActivityNoteListBinding
import com.felwal.stratomark.ui.notedetail.NoteDetailActivity
import com.felwal.stratomark.ui.setting.SettingsActivity
import com.felwal.stratomark.util.launchActivity
import kotlin.concurrent.thread

class NoteListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteListBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: NoteAdapter

    private val items: List<Note> get() = db.noteDao().getAllNotes()

    // Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tb)

        db = AppDatabase.getInstance(applicationContext)
        db.onWriteListener = { submitItems() }

        binding.rv.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.rv.canScrollVertically(-1)
        }

        initFab()
        initRecycler()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tb_note_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_settings -> launchActivity<SettingsActivity>()
            else -> super.onOptionsItemSelected(item)
        }

    // init

    private fun initFab() = binding.fab.setOnClickListener {
        NoteDetailActivity.startActivity(this)
    }

    private fun initRecycler() {
        adapter = NoteAdapter(
            onClick = { NoteDetailActivity.startActivity(this, it.noteId) },
            onLongClick = { selectNote(it) }
        )
        binding.rv.adapter = adapter
        binding.rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        submitItems()
    }

    //

    private fun selectNote(note: Note) {
        val copy = note.copy(noteId = NO_ID)
        // TODO
        thread {
            db.noteDao().addNote(copy)
            db.invokeWriteListener(this)
        }
    }

    private fun submitItems() = thread { adapter.submitList(items) }
}