package com.felwal.stratomark.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.felwal.stratomark.R
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.databinding.ActivityNoteDetailBinding
import com.felwal.stratomark.util.close
import android.text.Editable
import android.text.Layout
import android.util.Log
import com.felwal.stratomark.data.AppDatabase
import com.felwal.stratomark.util.copy
import com.felwal.stratomark.util.selectEnd
import com.felwal.stratomark.util.showKeyboard
import com.felwal.stratomark.util.string

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var db: AppDatabase

    private val etCurrentFocus: EditText?
        get() = if (currentFocus is EditText) currentFocus as EditText else null

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

        loadNote(0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tb_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> close()
            R.id.action_undo -> {} // TODO
            R.id.action_redo -> {} // TODO
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
        if (hasAnyFocus()) clearAllFocus()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        saveNote()
        super.onDestroy()
    }

    // focus

    private fun hasAnyFocus(): Boolean = binding.etTitle.hasFocus() || binding.etBody.hasFocus()

    private fun clearAllFocus() {
        binding.etTitle.clearFocus()
        binding.etBody.clearFocus()
    }

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

    private fun EditText.numberlist() = markSelectedLines { "${it+1}. " }

    private fun EditText.checklist() = markSelectedLines { "- [] " }

    private fun EditText.horizontalRule() = insert("* * *")

    // save

    private fun loadNote(id: Int) {
        Thread {
            val note = db.noteDao().get(id)
            if (note != null) {
                Log.i("db", "note: $note")
                runOnUiThread {
                    binding.etTitle.setText(note.titleWithExt)
                    binding.etBody.setText(note.body)
                }
            }
        }.start()
    }

    private fun saveNote(): Boolean {
        var title = binding.etTitle.string
        val body = binding.etBody.string

        val splits = title.split(".")
        val extension = if (splits.size >= 2) splits.last() else ""
        title = splits.first()

        val note = Note(0, title, body, extension)
        if (!note.isEmpty()) {
            Thread {
                Log.i("db", "note: $note")
                db.noteDao().addOrUpdate(note)
            }.start()
        }

        return true
    }
}