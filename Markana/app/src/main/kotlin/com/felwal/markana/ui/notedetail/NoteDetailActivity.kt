package com.felwal.markana.ui.notedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import com.felwal.markana.App
import com.felwal.markana.AppContainer
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.data.network.CreateTextDocument
import com.felwal.markana.databinding.ActivityNotedetailBinding
import com.felwal.markana.prefs
import com.felwal.markana.util.copyToClipboard
import com.felwal.markana.util.defaults
import com.felwal.markana.util.getIntegerArray
import com.felwal.markana.util.getQuantityString
import com.felwal.markana.util.indent
import com.felwal.markana.util.insertThematicBreak
import com.felwal.markana.util.makeMultilinePreventEnter
import com.felwal.markana.util.multiplyAlphaComponent
import com.felwal.markana.util.outdent
import com.felwal.markana.util.selectEnd
import com.felwal.markana.util.setOptionalIconsVisible
import com.felwal.markana.util.showKeyboard
import com.felwal.markana.util.string
import com.felwal.markana.util.then
import com.felwal.markana.util.toEpochSecond
import com.felwal.markana.util.toggleBulletList
import com.felwal.markana.util.toggleChecklist
import com.felwal.markana.util.toggleCode
import com.felwal.markana.util.toggleEmph
import com.felwal.markana.util.toggleHeader
import com.felwal.markana.util.toggleNumberList
import com.felwal.markana.util.toggleQuote
import com.felwal.markana.util.toggleStrikethrough
import com.felwal.markana.util.toggleStrong
import com.felwal.markana.util.updateDayNight
import com.felwal.markana.util.useEditHandler
import com.felwal.markana.widget.UndoRedoManager
import com.felwal.markana.widget.dialog.BinaryDialog
import com.felwal.markana.widget.dialog.ColorDialog
import com.felwal.markana.widget.dialog.binaryDialog
import com.felwal.markana.widget.dialog.colorDialog
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.CodeBlockSpan
import io.noties.markwon.core.spans.CodeSpan
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import java.time.LocalDateTime
import java.util.concurrent.Executors

private const val LOG_TAG = "NoteDetail"

private const val EXTRA_NOTE_URI = "uri"

private const val DIALOG_DELETE = "deleteNote"
private const val DIALOG_COLOR = "colorNote"

class NoteDetailActivity : AppCompatActivity(), BinaryDialog.DialogListener, ColorDialog.DialogListener {

    private lateinit var binding: ActivityNotedetailBinding

    private lateinit var appContainer: AppContainer
    private lateinit var model: NoteDetailViewModel

    private val etCurrentFocus: EditText?
        get() = if (currentFocus is EditText) currentFocus as EditText else null

    private val hasAnyFocus: Boolean
        get() = binding.etNoteTitle.hasFocus() || binding.etNoteBody.hasFocus()

    private val haveChangesBeenMade: Boolean
        get() = binding.etNoteTitle.string != model.note.filename || binding.etNoteBody.string != model.note.content

    private lateinit var contentHistoryManager: UndoRedoManager

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
        updateDayNight()
        super.onCreate(savedInstanceState)
        binding = ActivityNotedetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init tb
        setSupportActionBar(binding.tb)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // enable clicking on links
        // TODO: this also moves focus to start of et on scroll
        //binding.etNoteBody.movementMethod = LinkMovementMethod.getInstance()

        // bab menu listener
        binding.bab.setOnMenuItemClickListener(::onOptionsItemSelected)

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
            note?.let {
                applyNoteColor(it)
                //initMarkwon(it)
                loadContent(it)
                initUndoRedo(it)
            }
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
        menu.setOptionalIconsVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = true defaults when (item.itemId) {
        // tb
        android.R.id.home -> saveNote() then finish()
        R.id.action_undo -> contentHistoryManager.undo()
        R.id.action_redo -> contentHistoryManager.redo()
        R.id.action_color -> colorDialog(
            title = getString(R.string.dialog_title_color_notes),
            items = getIntegerArray(R.array.note_palette),
            checkedItem = model.note.colorIndex,
            tag = DIALOG_COLOR
        ).show(supportFragmentManager)
        R.id.action_clipboard -> copyToClipboard(binding.etNoteBody.string)
        R.id.action_delete -> binaryDialog(
            title = getQuantityString(R.plurals.dialog_title_delete_notes, 1),
            message = getString(R.string.dialog_msg_delete_notes),
            posBtnTxtRes = R.string.dialog_btn_delete,
            tag = DIALOG_DELETE
        ).show(supportFragmentManager)

        // bab
        R.id.action_bold -> etCurrentFocus?.toggleStrong()
        R.id.action_italic -> etCurrentFocus?.toggleEmph()
        R.id.action_strikethrough -> etCurrentFocus?.toggleStrikethrough()
        R.id.action_code -> etCurrentFocus?.toggleCode()
        R.id.action_heading -> etCurrentFocus?.toggleHeader()
        R.id.action_quote -> etCurrentFocus?.toggleQuote()
        R.id.action_bullet_list -> etCurrentFocus?.toggleBulletList()
        R.id.action_number_list -> etCurrentFocus?.toggleNumberList()
        R.id.action_checklist -> etCurrentFocus?.toggleChecklist()
        R.id.action_outdent -> etCurrentFocus?.outdent()
        R.id.action_indent -> etCurrentFocus?.indent()
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

    override fun onPause() {
        saveNote()
        super.onPause()
    }

    override fun onDestroy() {
        appContainer.noteDetailContainer = null
        super.onDestroy()
    }

    //

    private fun initMarkwon(note: Note) {
        val theme = MarkwonTheme.builderWithDefaults(this).run {
            build()
        }

        val markwon = Markwon.builder(this).run {
            usePlugin(SoftBreakAddsNewLinePlugin.create())
            build()
        }

        val editor = MarkwonEditor.builder(markwon).run {
            // change color
            punctuationSpan(ForegroundColorSpan::class.java) { ForegroundColorSpan(note.getColor(this@NoteDetailActivity)) }

            // emphasis
            useEditHandler(StrongEmphasisSpan(), "**", "__")
            useEditHandler(EmphasisSpan(), "*", "_")
            //useEditHandler(StrikethroughSpan(), "~~")

            // block
            //useEditHandler(QuoteSpan(), ">")
            useEditHandler(CodeBlockSpan(theme), "```")
            useEditHandler(CodeSpan(theme), "`")

            // heading
            //useEditHandler(HeadingSpan(theme, 6), "######")
            //useEditHandler(HeadingSpan(theme, 5), "#####")
            //useEditHandler(HeadingSpan(theme, 4), "####")
            //useEditHandler(HeadingSpan(theme, 3), "###")
            //useEditHandler(HeadingSpan(theme, 2), "##")
            //useEditHandler(HeadingSpan(theme, 1), "#")

            build()
        }

        binding.etNoteBody.addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(editor, Executors.newCachedThreadPool(), binding.etNoteBody)
        )
    }

    private fun initUndoRedo(note: Note) {
        val colorEnabled = note.getColor(this@NoteDetailActivity)
        val colorDisabled = colorEnabled.multiplyAlphaComponent(0.5f)
        val undoItem = binding.tb.menu.findItem(R.id.action_undo)
        val redoItem = binding.tb.menu.findItem(R.id.action_redo)

        undoItem.isEnabled = false
        undoItem.icon?.setTint(colorDisabled)
        redoItem.isEnabled = false
        redoItem.icon?.setTint(colorDisabled)

        contentHistoryManager = UndoRedoManager(binding.etNoteBody).apply {
            doOnCanUndoChange { canUndo ->
                undoItem.isEnabled = canUndo
                undoItem.icon?.setTint(if (canUndo) colorEnabled else colorDisabled)
            }
            doOnCanRedoChange { canRedo ->
                redoItem.isEnabled = canRedo
                redoItem.icon?.setTint(if (canRedo) colorEnabled else colorDisabled)
            }
        }
    }

    private fun clearAllFocus() {
        binding.etNoteTitle.clearFocus()
        binding.etNoteBody.clearFocus()
    }

    private fun loadContent(note: Note) {
        binding.etNoteTitle.setText(note.filename)
        binding.etNoteBody.setText(note.content)
    }

    private fun saveNote() {
        model.noteData.value ?: return

        val title = binding.etNoteTitle.string
        val body = binding.etNoteBody.string
        val now = LocalDateTime.now().toEpochSecond()
        val modified = if (haveChangesBeenMade) now else model.note.modified
        val note = model.note.copy(filename = title, content = body, modified = modified, opened = now)

        model.saveNote(note, title != model.note.filename)

        Log.i(LOG_TAG, "note saved: $note")
    }

    private fun applyNoteColor(note: Note) {
        val fgColor = note.getColor(this)
        val bgColor = note.getBackgroundColor(this).multiplyAlphaComponent(0.65f)

        binding.etNoteTitle.setTextColor(fgColor)
        binding.tb.menu.forEach { it.icon?.setTint(fgColor) }
        binding.bab.menu.forEach { it.icon?.setTint(fgColor) }

        if (prefs.notePreviewColor) {
            //binding.root.setBackgroundColor(bgColor)
            //binding.tb.setBackgroundColor(bgColor)
            binding.bab.setBackgroundColor(bgColor)
        }
    }

    // dialog

    override fun onBinaryDialogPositiveClick(passValue: String?, tag: String) {
        when (tag) {
            DIALOG_DELETE -> model.deleteNote() then finish()
        }
    }

    override fun onColorDialogItemClick(checkedItem: Int, tag: String) {
        when (tag) {
            DIALOG_COLOR -> {
                model.note.colorIndex = checkedItem
                applyNoteColor(model.note)
            }
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