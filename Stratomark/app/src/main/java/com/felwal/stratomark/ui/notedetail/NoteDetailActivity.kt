package com.felwal.stratomark.ui.notedetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.felwal.stratomark.R
import com.felwal.stratomark.data.AppDatabase
import com.felwal.stratomark.data.NO_ID
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.data.NoteRepository
import com.felwal.stratomark.data.URI_DEFAULT
import com.felwal.stratomark.databinding.ActivityNoteDetailBinding
import com.felwal.stratomark.network.SafHelper
import com.felwal.stratomark.util.copy
import com.felwal.stratomark.util.copyToClipboard
import com.felwal.stratomark.util.defaults
import com.felwal.stratomark.util.selectEnd
import com.felwal.stratomark.util.showKeyboard
import com.felwal.stratomark.util.string
import com.felwal.stratomark.util.then
import kotlin.concurrent.thread

private const val LOG_TAG = "NoteDetail"
private const val EXTRA_NOTE_URI = "uri"
private const val EXTRA_NOTE_ID = "id"

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding

    private lateinit var db: AppDatabase
    private lateinit var saf: SafHelper
    private lateinit var repo: NoteRepository

    private var noteUri: String? = null
    private var noteId: Int? = null
    private var initialTitle = ""
    private var initialBody = ""

    private val etCurrentFocus: EditText?
        get() = if (currentFocus is EditText) currentFocus as EditText else null

    private val hasAnyFocus: Boolean
        get() = binding.etNoteTitle.hasFocus() || binding.etNoteBody.hasFocus()

    private val haveChangesBeenMade: Boolean
        get() = binding.etNoteTitle.string != initialTitle || binding.etNoteBody.string != initialBody

    // Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tb)

        // tb: title and home
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // data
        db = AppDatabase.getInstance(applicationContext)
        saf = SafHelper(this) { uri ->
            uri ?: finish() // if user didn't pick a location, cancel
            noteUri = uri.toString()
        }
        repo = NoteRepository(db, saf)

        // bab menu listener
        binding.babTypography.setOnMenuItemClickListener(::onOptionsItemSelected)

        // focus body on outside click
        binding.vEmpty.setOnClickListener {
            binding.etNoteBody.showKeyboard()
            binding.etNoteBody.selectEnd()
        }

        // set up uri and load or create file
        noteUri = intent.getStringExtra(EXTRA_NOTE_URI)
        noteUri?.let {
            thread {
                val note = repo.getNote(it)
                note ?: finish() // the note was not found or had not granted access
                note?.let { runOnUiThread { loadNote(it) } }
            }
        }
        noteUri ?: repo.createNote()

        // animate tb elevation on scroll
        binding.nsvNote.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.nsvNote.canScrollVertically(-1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tb_note_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = true defaults when (item.itemId) {
        // tb
        android.R.id.home -> saveNote() then finish()
        R.id.action_undo -> {} // TODO
        R.id.action_redo -> {} // TODO
        R.id.action_clipboard -> copyToClipboard(binding.etNoteBody.string)
        R.id.action_delete -> deleteNote() then finish() then true

        // bab
        R.id.action_bold -> etCurrentFocus?.bold()
        R.id.action_italic -> etCurrentFocus?.italic()
        R.id.action_strikethrough -> etCurrentFocus?.strikethrough()
        R.id.action_heading -> etCurrentFocus?.header()
        R.id.action_checklist -> etCurrentFocus?.checklist()
        R.id.action_quote -> etCurrentFocus?.quote()
        R.id.action_code -> etCurrentFocus?.code()
        R.id.action_bulletlist -> etCurrentFocus?.bulletlist()
        R.id.action_numberlist -> etCurrentFocus?.numberlist()
        R.id.action_scenebreak -> etCurrentFocus?.horizontalRule()

        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (hasAnyFocus) clearAllFocus()
        else {
            saveNote()
            super.onBackPressed()
        }
    }

    // focus

    private fun clearAllFocus() {
        binding.etNoteTitle.clearFocus()
        binding.etNoteBody.clearFocus()
    }

    // view

    private fun loadNote(note: Note) {
        binding.etNoteTitle.setText(note.filename)
        binding.etNoteBody.setText(note.content)

        initialTitle = note.filename
        initialBody = note.content
    }

    // data

    private fun saveNote() {
        if (!haveChangesBeenMade) return

        val title = binding.etNoteTitle.string
        val body = binding.etNoteBody.string
        val note = Note(title, body, noteUri ?: URI_DEFAULT, noteId ?: NO_ID)

        Log.i(LOG_TAG, "note saved: $note")

        thread {
            repo.saveNote(note, title != initialTitle)
            db.invokeWriteListener(this)
        }
    }

    private fun deleteNote() = noteUri?.let { repo.deleteNote(it) }

    // typography bar generals

    private fun EditText.insertAtCursor(marker: String) {
        val textEdit = text.copy
        val end = selectionEnd

        textEdit.insert(end, marker)

        text = textEdit
        setSelection(end + marker.length)
    }

    private fun EditText.markSelectedText(marker: String) {
        val textEdit = text.copy
        val end = selectionEnd
        val start = selectionStart

        textEdit.insert(end, marker)
        textEdit.insert(start, marker)

        // TODO: check for empty line

        // TODO: toggle

        // TODO: remove markers symmetrically

        text = textEdit
        setSelection(start + marker.length, end + marker.length)
    }

    private fun EditText.markSelectedLines(marker: (Int) -> String) {
        val textEdit = text.copy
        val end = selectionEnd
        val start = selectionStart

        val endLine = layout.getLineForOffset(end)
        val startLine = layout.getLineForOffset(start)

        var endOffset = 0
        for (line in endLine downTo startLine) {
            val lineStart = layout.getLineStart(line)
            val lineIndex = line - startLine

            textEdit.insert(lineStart, marker(lineIndex))
            endOffset += marker(lineIndex).length
        }

        // TODO: toggle (also between different lists)

        text = textEdit
        setSelection(start + marker(startLine).length, end + endOffset)
    }

    // typography bar specifics

    private fun EditText.italic() = markSelectedText("_")

    private fun EditText.bold() = markSelectedText("**")

    private fun EditText.strikethrough() = markSelectedText("~~")

    private fun EditText.code() = markSelectedText("`")

    private fun EditText.header() = markSelectedLines { "# " }

    private fun EditText.quote() = markSelectedLines { "> " }

    private fun EditText.bulletlist() = markSelectedLines { "* " }

    private fun EditText.numberlist() = markSelectedLines { "${it + 1}. " }

    private fun EditText.checklist() = markSelectedLines { "- [] " }

    private fun EditText.horizontalRule() = insertAtCursor("* * *")

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