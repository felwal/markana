package com.felwal.markana.ui.setting

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import com.felwal.android.ui.AbsSettingsActivity
import com.felwal.android.util.getQuantityString
import com.felwal.android.widget.dialog.DecimalDialog
import com.felwal.android.widget.dialog.SingleChoiceDialog
import com.felwal.android.widget.dialog.TextDialog
import com.felwal.markana.R
import com.felwal.markana.data.prefs.Bullet
import com.felwal.markana.data.prefs.Emph
import com.felwal.markana.data.prefs.Strong
import com.felwal.markana.data.prefs.Theme
import com.felwal.markana.databinding.ActivitySettingsBinding
import com.felwal.markana.prefs
import com.felwal.markana.util.checkbox
import com.felwal.markana.util.updateDayNight

private const val DIALOG_THEME = "themeDialog"
private const val DIALOG_MAX_LINES = "maxLinesDialog"
private const val DIALOG_ITALIC = "italicDialog"
private const val DIALOG_BOLD = "boldDialog"
private const val DIALOG_BULLET_LIST = "bulletListDialog"
private const val DIALOG_HR = "hrDialog"

class SettingsActivity :
    AbsSettingsActivity(dividerMode = DividerMode.IN_SECTION, indentEverything = false),
    SingleChoiceDialog.DialogListener,
    TextDialog.DialogListener,
    DecimalDialog.DialogListener {

    // view
    private lateinit var binding: ActivitySettingsBinding
    override val llItemContainer: LinearLayout get() = binding.ll

    // lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        updateDayNight()
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        inflateSettingItems()
    }

    // menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // view

    private fun initViews() {
        // init tb
        setSupportActionBar(binding.tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // animate tb elevation on scroll
        binding.nsv.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.nsv.canScrollVertically(-1)
        }
    }

    override fun inflateSettingItems() {
        inflateSections(
            ItemSection(
                title = getString(R.string.tv_settings_header_appearance),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_theme),
                    values = Theme.values().map { it.title },
                    selectedIndex = prefs.themeInt,
                    iconRes = R.drawable.ic_theme_24,
                    tag = DIALOG_THEME
                )
            ),
            ItemSection(
                title = getString(R.string.tv_settings_header_notepreview),
                BooleanItem(
                    title = getString(R.string.tv_settings_item_title_preview_color),
                    value = prefs.notePreviewColor,
                    iconRes = R.drawable.ic_color_24,
                    onSwitch = { prefs.notePreviewColor = !prefs.notePreviewColor }
                ),
                BooleanItem(
                    title = getString(R.string.tv_settings_item_title_preview_icon),
                    value = prefs.notePreviewListIcon,
                    iconRes = R.drawable.ic_note_24,
                    onSwitch = { prefs.notePreviewListIcon = !prefs.notePreviewListIcon }
                ),
                BooleanItem(
                    title = getString(R.string.tv_settings_item_title_preview_metadata),
                    value = prefs.notePreviewMetadata,
                    iconRes = R.drawable.ic_calendar_24,
                    onSwitch = { prefs.notePreviewMetadata = !prefs.notePreviewMetadata }
                ),
                BooleanItem(
                    title = getString(R.string.tv_settings_item_title_preview_mime),
                    descOn = "filename.md",
                    descOff = "filename",
                    value = prefs.notePreviewMime,
                    iconRes = R.drawable.ic_file_24,
                    onSwitch = { prefs.notePreviewMime = !prefs.notePreviewMime }
                ),
                SliderItem(
                    title = getString(R.string.tv_settings_item_title_preview_max_lines),
                    desc = getQuantityString(
                        R.plurals.tv_settings_item_desc_preview_max_lines,
                        prefs.notePreviewMaxLines
                    ),
                    min = 0f,
                    max = 20f,
                    step = 1f,
                    value = prefs.notePreviewMaxLines.toFloat(),
                    iconRes = R.drawable.ic_line_spacing_24,
                    tag = DIALOG_MAX_LINES
                )
            ),
            ItemSection(
                title = getString(R.string.tv_settings_header_markdown),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_italic_symbol),
                    values = Emph.values().map { it.title },
                    selectedIndex = prefs.emphSymbolInt,
                    iconRes = R.drawable.ic_italic_24,
                    tag = DIALOG_ITALIC
                ),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_bold_symbol),
                    values = Strong.values().map { it.title },
                    selectedIndex = prefs.strongSymbolInt,
                    iconRes = R.drawable.ic_bold_24,
                    tag = DIALOG_BOLD
                ),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_bullet_list_symbol),
                    values = Bullet.values().map { it.title },
                    selectedIndex = prefs.bulletListSymbolInt,
                    iconRes = R.drawable.ic_list_bullet_24,
                    tag = DIALOG_BULLET_LIST
                ),
                StringItem(
                    title = getString(R.string.tv_settings_item_title_hr_symbol),
                    value = prefs.breakSymbol,
                    hint = "3 or more of *, -, or _",
                    iconRes = R.drawable.ic_horizontal_rule_24,
                    tag = DIALOG_HR
                ),
                BooleanItem(
                    title = getString(R.string.tv_settings_item_title_checkbox_space),
                    value = prefs.checkboxSpace,
                    descOn = checkbox(checked = false, emptySpace = true),
                    descOff = checkbox(checked = false, emptySpace = false),
                    iconRes = R.drawable.ic_checkbox_blank_24,
                    onSwitch = { prefs.checkboxSpace = !prefs.checkboxSpace }
                ),
            ),
            ItemSection(
                title = "About and other",
                InfoItem(
                    getString(R.string.tv_settings_item_title_about),
                    msg = getString(R.string.tv_settings_item_msg_about),
                    dialogBtnRes = R.string.fw_dialog_btn_ok,
                    tag = "aboutDialog"
                )
            )
        )
    }

    // dialog

    override fun onSingleChoiceDialogItemSelected(selectedIndex: Int, tag: String) {
        when (tag) {
            DIALOG_THEME -> {
                prefs.themeInt = selectedIndex
                updateDayNight()
            }
            DIALOG_ITALIC -> prefs.emphSymbolInt = selectedIndex
            DIALOG_BOLD -> prefs.strongSymbolInt = selectedIndex
            DIALOG_BULLET_LIST -> prefs.bulletListSymbolInt = selectedIndex
        }
        reflateViews()
    }

    override fun onTextDialogPositiveClick(input: String, tag: String) {
        when (tag) {
            DIALOG_HR -> prefs.breakSymbol = input
        }
        reflateViews()
    }

    override fun onDecimalDialogPositiveClick(input: Float, tag: String) {
        when (tag) {
            DIALOG_MAX_LINES -> prefs.notePreviewMaxLines = input.toInt()
        }
        reflateViews()
    }
}