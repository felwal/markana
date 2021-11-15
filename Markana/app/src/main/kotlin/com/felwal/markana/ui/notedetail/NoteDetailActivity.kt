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
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import com.felwal.android.util.coerceSelection
import com.felwal.android.util.copyToClipboard
import com.felwal.android.util.getColorAttr
import com.felwal.android.util.getIntegerArray
import com.felwal.android.util.getQuantityString
import com.felwal.android.util.makeMultilinePreventEnter
import com.felwal.android.util.multiplyAlphaComponent
import com.felwal.android.util.searchView
import com.felwal.android.util.selectEnd
import com.felwal.android.util.setOptionalIconsVisible
import com.felwal.android.util.showKeyboard
import com.felwal.android.util.string
import com.felwal.android.util.toast
import com.felwal.android.widget.dialog.AlertDialog
import com.felwal.android.widget.dialog.SingleChoiceDialog
import com.felwal.android.widget.dialog.alertDialog
import com.felwal.android.widget.dialog.colorDialog
import com.felwal.markana.App
import com.felwal.markana.AppContainer
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.data.network.CreateTextDocument
import com.felwal.markana.databinding.ActivityNotedetailBinding
import com.felwal.markana.prefs
import com.felwal.markana.util.getResIdArray
import com.felwal.markana.util.indent
import com.felwal.markana.util.insertThematicBreak
import com.felwal.markana.util.outdent
import com.felwal.markana.util.setCloseIcon
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

private const val EXTRA_COLOR_INDEX = "colorIndex"
private const val EXTRA_NOTE_URI = "uri"
private const val EXTRA_FIND_QUERY = "findQuery"

private const val DIALOG_DELETE = "deleteNote"
private const val DIALOG_COLOR = "colorNote"

class NoteDetailActivity :
    AppCompatActivity(),
    AlertDialog.DialogListener,
    SingleChoiceDialog.DialogListener {

    // data
    private lateinit var appContainer: AppContainer
    private lateinit var model: NoteDetailViewModel

    // view
    private lateinit var binding: ActivityNotedetailBinding
    private lateinit var contentHistoryManager: UndoRedoManager

    // saf result launcher
    private val createDocument = registerForActivityResult(CreateTextDocument()) { uri ->
        // the user didn't pick a location; cancel activity
        uri ?: finish().also { return@registerForActivityResult }
        Log.i(LOG_TAG, "create document uri result: $uri")

        model.handleCreatedDocument(uri)
    }

    // lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        updateDayNight()
        initTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityNotedetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initViews()
        initData()
    }

    override fun onBackPressed() {
        if (binding.etNoteTitle.hasFocus() || binding.etNoteBody.hasFocus()) clearAllFocus()
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

    // menu

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_notedetail_tb, menu)

        // find in note
        val findItem = menu.findItem(R.id.action_find).apply {
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    setFindInNoteMode(true)
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    setFindInNoteMode(false)
                    return true
                }
            })
        }
        findItem.searchView.apply {
            // set hint (xml attribute doesn't work)
            queryHint = getString(R.string.tv_notedetail_find_hint)

            // set close icon (the default is not of 'round' style)
            setCloseIcon(R.drawable.ic_close_24)

            // set listener
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    model.findInNote(query, binding.etNoteBody.string)

                    toast(
                        getQuantityString(
                            R.plurals.toast_i_occurrences_found,
                            model.findInNoteOccurrenceIndices.size
                        )
                    )
                    selectFindInNoteOccurrence()

                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean = true
            })
        }

        menu.setOptionalIconsVisible(true)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val etCurrentFocus = currentFocus as? EditText

        when (item.itemId) {
            // normal tb
            android.R.id.home -> {
                if (model.isFindInNoteMode) setFindInNoteMode(false)
                else {
                    saveNote()
                    finish()
                }
            }
            R.id.action_undo -> contentHistoryManager.undo()
            R.id.action_redo -> contentHistoryManager.redo()
            R.id.action_color -> colorDialog(
                title = getString(R.string.dialog_title_color_notes),
                colors = getIntegerArray(R.array.note_palette),
                checkedIndex = model.note.colorIndex,
                tag = DIALOG_COLOR
            ).show(supportFragmentManager)
            R.id.action_find_previous -> selectPreviousFindInNoteOccurrence()
            R.id.action_find_next -> selectNextFindInNoteOccurrence()
            R.id.action_clipboard -> copyToClipboard(binding.etNoteBody.string)
            R.id.action_delete -> alertDialog(
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

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //

    private fun initTheme() {
        val themes = getResIdArray(R.array.note_themes)
        val theme = themes[intent.getIntExtra(EXTRA_COLOR_INDEX, 0)]
        setTheme(theme)
    }

    // data

    private fun initData() {
        appContainer = (application as App).appContainer
        appContainer.noteDetailContainer = NoteDetailContainer(appContainer.noteRepository)
        model = appContainer.noteDetailContainer!!.noteDetailViewModel

        // set up uri and load / create file
        model.noteUri = intent.getStringExtra(EXTRA_NOTE_URI)
        model.noteData.observe(this) { note ->
            note ?: finish() // the note was not found or had not granted access
            note?.let {
                //initMarkwon(it)
                loadContent(it)
                // init undo redo after loaded content to ignore first text load
                initUndoRedo()
            }
        }
        model.noteUri ?: model.createNote(createDocument)
        model.loadNote()
    }

    // edittext

    private fun initViews() {
        binding.etNoteTitle.makeMultilinePreventEnter()

        // enable clicking on links
        // TODO: this also moves focus to start of et on scroll
        //binding.etNoteBody.movementMethod = LinkMovementMethod.getInstance()

        // focus body on outside click
        binding.vEmpty.setOnClickListener {
            binding.etNoteBody.showKeyboard()
            binding.etNoteBody.selectEnd()
        }

        // bab menu listener
        binding.bab.setOnMenuItemClickListener(::onOptionsItemSelected)

        // animate tb elevation on scroll
        binding.nsvNote.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.nsvNote.canScrollVertically(-1)
        }
    }

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
            /*punctuationSpan(ForegroundColorSpan::class.java) {
                ForegroundColorSpan(note.getColor(this@NoteDetailActivity))
            }*/

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

    private fun loadContent(note: Note) {
        val isInitialLoad = binding.etNoteTitle.string == "" && binding.etNoteBody.string == ""

        // set content
        binding.etNoteTitle.setText(note.filename)
        binding.etNoteBody.setText(note.content)

        if (isInitialLoad) {
            // find action: get findQuery extra, open search view and fire search
            intent.getStringExtra(EXTRA_FIND_QUERY)?.let { query ->
                binding.tb.menu.findItem(R.id.action_find).apply {
                    expandActionView()
                    searchView.setQuery(query, true)
                }
            }
            // find action: collapse find on text changed to avoid out-of-date selection
            binding.etNoteBody.setOnClickListener {
                binding.tb.menu
                    .findItem(R.id.action_find)
                    .collapseActionView()
                // collapseActionView hides keyboard; show it again
                binding.etNoteBody.showKeyboard()
            }
        }
    }

    private fun initUndoRedo() {
        val colorEnabled = getColorAttr(R.attr.colorControlActivated)
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

    private fun saveNote() {
        model.noteData.value ?: return
        val haveChangesBeenMade =
            binding.etNoteTitle.string != model.note.filename
                || binding.etNoteBody.string != model.note.content

        val title = binding.etNoteTitle.string
        val body = binding.etNoteBody.string
        val now = LocalDateTime.now().toEpochSecond()
        val modified = if (haveChangesBeenMade) now else model.note.modified
        val note = model.note.copy(filename = title, content = body, modified = modified, opened = now)

        model.saveNote(note)

        Log.i(LOG_TAG, "note saved: $note")
    }

    private fun clearAllFocus() {
        binding.etNoteTitle.clearFocus()
        binding.etNoteBody.clearFocus()
    }

    // toolbar

    private fun initToolbar() {
        setSupportActionBar(binding.tb)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setFindInNoteMode(enabled: Boolean) {
        model.isFindInNoteMode = enabled

        // remove selection
        if (!enabled) binding.etNoteBody.clearFocus()

        // update tb
        binding.tb.menu.run {
            findItem(R.id.action_find_previous).isVisible = enabled
            findItem(R.id.action_find_next).isVisible = enabled

            findItem(R.id.action_undo).isVisible = !enabled
            findItem(R.id.action_redo).isVisible = !enabled
            findItem(R.id.action_color).isVisible = !enabled
            findItem(R.id.action_clipboard).isVisible = !enabled
            findItem(R.id.action_delete).isVisible = !enabled
        }
    }

    // find in note

    private fun selectPreviousFindInNoteOccurrence() {
        model.decFindInNoteCursor()
        selectFindInNoteOccurrence()
    }

    private fun selectNextFindInNoteOccurrence() {
        model.incFindInNoteCursor()
        selectFindInNoteOccurrence()
    }

    private fun selectFindInNoteOccurrence() {
        if (model.findInNoteOccurrenceIndices.isEmpty()) return

        val index = model.findInNoteOccurrenceIndex
        binding.etNoteBody.requestFocus()
        binding.etNoteBody.coerceSelection(index, index + model.findInNoteQueryLength)
    }

    // dialog

    override fun onAlertDialogPositiveClick(passValue: String?, tag: String) {
        when (tag) {
            DIALOG_DELETE -> {
                model.deleteNote()
                finish()
            }
        }
    }

    override fun onSingleChoiceDialogItemSelected(selectedIndex: Int, tag: String) {
        when (tag) {
            DIALOG_COLOR -> {
                model.note.colorIndex = selectedIndex
                // recreate to apply new theme
                intent.putExtra(EXTRA_COLOR_INDEX, selectedIndex)
                recreate()
            }
        }
    }

    // object

    companion object {
        fun startActivity(c: Context, uri: String? = null, colorIndex: Int = 0, findQuery: String? = null) {
            val intent = Intent(c, NoteDetailActivity::class.java)

            uri?.let { intent.putExtra(EXTRA_NOTE_URI, it) }
            intent.putExtra(EXTRA_COLOR_INDEX, colorIndex)
            findQuery?.let { intent.putExtra(EXTRA_FIND_QUERY, it) }

            c.startActivity(intent)
        }
    }
}