package com.felwal.stratomark.ui.notedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.felwal.stratomark.R
import com.felwal.stratomark.data.AppDatabase
import com.felwal.stratomark.data.NO_ID
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.databinding.ActivityNoteDetailBinding
import com.felwal.stratomark.util.close
import com.felwal.stratomark.util.copy
import com.felwal.stratomark.util.selectEnd
import com.felwal.stratomark.util.showKeyboard
import com.felwal.stratomark.util.string
import kotlin.concurrent.thread

private const val EXTRA_NOTE_ID = "id"

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var db: AppDatabase
    private var noteId: Int? = null

    private val etCurrentFocus: EditText?
        get() = if (currentFocus is EditText) currentFocus as EditText else null

    private val hasAnyFocus: Boolean
        get() = binding.etTitle.hasFocus() || binding.etBody.hasFocus()

    // Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tb)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getInstance(applicationContext)

        // bab menu
        binding.babTypography.setOnMenuItemClickListener(::onOptionsItemSelected)

        // focus body on outside click
        binding.vEmpty.setOnClickListener {
            binding.etBody.showKeyboard()
            binding.etBody.selectEnd()
        }

        noteId = intent.getIntExtra(EXTRA_NOTE_ID)
        noteId?.let { loadNote(it) }

        binding.nsv.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.nsv.canScrollVertically(-1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tb_note_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // tb
            android.R.id.home -> {
                saveNote()
                close()
            }
            R.id.action_undo -> {} // TODO
            R.id.action_redo -> {} // TODO
            R.id.action_delete -> {
                deleteNote()
                close()
            }

            // bab
            R.id.action_bold -> etCurrentFocus?.bold()
            R.id.action_italic -> etCurrentFocus?.italic()
            R.id.action_strikethrough -> etCurrentFocus?.strikethrough()
            R.id.action_heading -> etCurrentFocus?.header()
            R.id.action_checkbox -> etCurrentFocus?.checklist()
            R.id.action_quote -> etCurrentFocus?.quote()
            R.id.action_code -> etCurrentFocus?.code()
            R.id.action_bulleted_list -> etCurrentFocus?.bulletlist()
            R.id.action_numbered_list -> etCurrentFocus?.numberlist()
            R.id.action_scenebreak -> etCurrentFocus?.horizontalRule()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
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
        binding.etTitle.clearFocus()
        binding.etBody.clearFocus()
    }

    // save/load

    private fun loadNote(noteId: Int) {
        thread {
            val note = db.noteDao().getNote(noteId)

            note?.let {
                Log.i("db", "note loaded: $it")
                runOnUiThread {
                    binding.etTitle.setText(it.titleWithExt)
                    binding.etBody.setText(it.body)
                }
            }
        }
    }

    private fun saveNote(): Boolean {
        var title = binding.etTitle.string
        val body = binding.etBody.string

        val splits = title.split(".")
        val extension = if (splits.size >= 2) splits.last() else ""
        title = splits.first()

        val note = Note(title, body, extension, noteId ?: NO_ID)
        if (note.isEmpty()) return true

        thread {
            Log.i("db", "note saved: $note")
            db.noteDao().addOrUpdateNote(note)
            db.invokeWriteListener(this)
        }

        return true
    }

    private fun deleteNote() = noteId?.let { thread {
        db.noteDao().deleteNote(it)
        db.invokeWriteListener(this)
    } }

    // typography bar generals

    private fun EditText.insert(marker: String) {
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

        // TODO: toggle

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

    private fun EditText.horizontalRule() = insert("* * *")

    //

    companion object {

        fun startActivity(c: Context, id: Int? = null) {
            val intent = Intent(c, NoteDetailActivity::class.java)
            id?.let { intent.putExtra(EXTRA_NOTE_ID, it); }
            c.startActivity(intent)
        }
    }
}

private fun Intent.getIntExtra(extraId: String): Int? {
    val extra = getIntExtra(extraId, -1)
    return if (extra == -1) null else extra
}