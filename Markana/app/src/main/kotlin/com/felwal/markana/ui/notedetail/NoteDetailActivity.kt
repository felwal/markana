package com.felwal.markana.ui.notedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.felwal.markana.AppContainer
import com.felwal.markana.App
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.databinding.ActivityNotedetailBinding
import com.felwal.markana.widget.dialog.BinaryDialog
import com.felwal.markana.widget.dialog.binaryDialog
import com.felwal.markana.data.network.CreateTextDocument
import com.felwal.markana.util.toggleStrong
import com.felwal.markana.util.toggleBulletlist
import com.felwal.markana.util.toggleChecklist
import com.felwal.markana.util.toggleCode
import com.felwal.markana.util.copyToClipboard
import com.felwal.markana.util.defaults
import com.felwal.markana.util.toggleHeader
import com.felwal.markana.util.insertThematicBreak
import com.felwal.markana.util.toggleEmph
import com.felwal.markana.util.makeMultilinePreventEnter
import com.felwal.markana.util.toggleNumberlist
import com.felwal.markana.util.toggleQuote
import com.felwal.markana.util.selectEnd
import com.felwal.markana.util.showKeyboard
import com.felwal.markana.util.toggleStrikethrough
import com.felwal.markana.util.string
import com.felwal.markana.util.then
import com.felwal.markana.util.toEpochSecond
import com.felwal.markana.util.updateTheme
import java.time.LocalDateTime

private const val LOG_TAG = "NoteDetail"

private const val EXTRA_NOTE_URI = "uri"

private const val DIALOG_DELETE = "deleteNotes"

class NoteDetailActivity : AppCompatActivity(), BinaryDialog.DialogListener {

    private lateinit var binding: ActivityNotedetailBinding

    private lateinit var appContainer: AppContainer
    private lateinit var model: NoteDetailViewModel

    private val etCurrentFocus: EditText?
        get() = if (currentFocus is EditText) currentFocus as EditText else null

    private val hasAnyFocus: Boolean
        get() = binding.etNoteTitle.hasFocus() || binding.etNoteBody.hasFocus()

    private val haveChangesBeenMade: Boolean
        get() = binding.etNoteTitle.string != model.note.filename || binding.etNoteBody.string != model.note.content

    private val createDocument = registerForActivityResult(CreateTextDocument()) { uri ->
        Log.i(LOG_TAG, "create document uri result: $uri")

        // the user didn't pick a location; cancel activity
        if (uri == null) {
            finish()
            return@registerForActivityResult
        }

        // save the note
        model.persistNotePermissions(uri)
        model.saveNote(Note(uri = uri.toString()), false)
        model.syncNote(uri.toString()) // sync to put filename in db before loading

        // load the note
        model.noteUri = uri.toString()
        model.loadNote()
    }

    // Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        updateTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityNotedetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init tb
        setSupportActionBar(binding.tb)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // bab menu listener
        binding.babTypography.setOnMenuItemClickListener(::onOptionsItemSelected)

        // focus body on outside click
        binding.vEmpty.setOnClickListener {
            binding.etNoteBody.showKeyboard()
            binding.etNoteBody.selectEnd()
        }

        // data
        appContainer = (application as App).appContainer
        appContainer.noteDetailContainer = NoteDetailContainer(appContainer.noteRepository)
        model = appContainer.noteDetailContainer!!.noteDetailViewModel

        // set up uri and load / create file
        model.noteUri = intent.getStringExtra(EXTRA_NOTE_URI)
        model.noteData.observe(this) { note ->
            note ?: finish() // the note was not found or had not granted access
            note?.let { loadContent(it) }
        }
        model.noteUri ?: model.createNote(createDocument)
        model.loadNote()

        // animate tb elevation on scroll
        binding.nsvNote.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.nsvNote.canScrollVertically(-1)
        }

        binding.etNoteTitle.makeMultilinePreventEnter()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_notedetail_tb, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = true defaults when (item.itemId) {
        // tb
        android.R.id.home -> saveNote() then finish()
        R.id.action_undo -> {} // TODO
        R.id.action_redo -> {} // TODO
        R.id.action_clipboard -> copyToClipboard(binding.etNoteBody.string)
        R.id.action_delete -> binaryDialog(
            title = getString(R.string.dialog_title_delete_note),
            message = getString(R.string.dialog_msg_delete_notes),
            posBtnTxtRes = R.string.dialog_btn_delete,
            tag = DIALOG_DELETE
        ).show(supportFragmentManager)

        // bab
        R.id.action_italic -> etCurrentFocus?.toggleEmph()
        R.id.action_bold -> etCurrentFocus?.toggleStrong()
        R.id.action_strikethrough -> etCurrentFocus?.toggleStrikethrough()
        R.id.action_heading -> etCurrentFocus?.toggleHeader()
        R.id.action_checklist -> etCurrentFocus?.toggleChecklist()
        R.id.action_quote -> etCurrentFocus?.toggleQuote()
        R.id.action_code -> etCurrentFocus?.toggleCode()
        R.id.action_bulletlist -> etCurrentFocus?.toggleBulletlist()
        R.id.action_numberlist -> etCurrentFocus?.toggleNumberlist()
        R.id.action_scenebreak -> etCurrentFocus?.insertThematicBreak()

        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (hasAnyFocus) clearAllFocus()
        else {
            saveNote()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        appContainer.noteDetailContainer = null
        super.onDestroy()
    }

    //

    private fun clearAllFocus() {
        binding.etNoteTitle.clearFocus()
        binding.etNoteBody.clearFocus()
    }

    private fun loadContent(note: Note) {
        binding.etNoteTitle.setText(note.filename)
        binding.etNoteBody.setText(note.content)
    }

    private fun saveNote() {
        val title = binding.etNoteTitle.string
        val body = binding.etNoteBody.string
        val now = LocalDateTime.now().toEpochSecond()
        val modified = if (haveChangesBeenMade) now else model.note.modified
        val note = model.note.copy(filename = title, content = body, modified = modified, opened = now)

        model.saveNote(note, title != model.note.filename)

        Log.i(LOG_TAG, "note saved: $note")
    }

    // dialog

    override fun onBinaryDialogPositiveClick(passValue: String?, tag: String) {
        when (tag) {
            DIALOG_DELETE -> model.deleteNote() then finish()
        }
    }

    //

    companion object {
        fun startActivity(c: Context, uri: String? = null) {
            val intent = Intent(c, NoteDetailActivity::class.java)
            uri?.let { intent.putExtra(EXTRA_NOTE_URI, it); }
            c.startActivity(intent)
        }
    }
}

private fun Intent.getIntExtra(extraId: String): Int? {
    val extra = getIntExtra(extraId, -1)
    return if (extra == -1) null else extra
}