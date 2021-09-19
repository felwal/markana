package com.felwal.markana.ui.setting

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import com.felwal.markana.R
import com.felwal.markana.data.prefs.Bullet
import com.felwal.markana.data.prefs.Emph
import com.felwal.markana.data.prefs.Strong
import com.felwal.markana.data.prefs.Theme
import com.felwal.markana.databinding.ActivitySettingsBinding
import com.felwal.markana.prefs
import com.felwal.markana.util.getQuantityString
import com.felwal.markana.util.updateDayNight
import com.felwal.markana.widget.dialog.NumberDialog
import com.felwal.markana.widget.dialog.RadioDialog
import com.felwal.markana.widget.dialog.TextDialog
import com.felwal.markana.widget.dialog.UnaryDialog

private const val DIALOG_THEME = "themeDialog"
private const val DIALOG_MAX_LINES = "maxLinesDialog"
private const val DIALOG_ITALIC = "italicDialog"
private const val DIALOG_BOLD = "boldDialog"
private const val DIALOG_BULLET_LIST = "bulletListDialog"
private const val DIALOG_HR = "hrDialog"

open class SettingsActivity : AbsSettingsActivity(
    dividerMode = DividerMode.IN_SECTION,
    indentEverything = false
),
    RadioDialog.DialogListener,
    TextDialog.DialogListener,
    NumberDialog.DialogListener,
    UnaryDialog.DialogListener {

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
                ),
                BooleanItem(
                    title = getString(R.string.tv_settings_item_title_preview_color),
                    value = prefs.notePreviewColor,
                    iconRes = R.drawable.ic_color_24,
                    onSwitch = { prefs.notePreviewColor = !prefs.notePreviewColor }
                ),
                IntItem(
                    title = getString(R.string.tv_settings_item_title_preview_max_lines),
                    desc = getQuantityString(
                        R.plurals.tv_settings_item_desc_preview_max_lines,
                        prefs.notePreviewMaxLines
                    ),
                    value = prefs.notePreviewMaxLines,
                    hint = "Default: 12",
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
                    descOn = "- [ ]",
                    descOff = "- []",
                    iconRes = R.drawable.ic_checkbox_blank_24,
                    onSwitch = { prefs.checkboxSpace = !prefs.checkboxSpace }
                ),
            ),
            ItemSection(
                title = "About and other",
                InfoItem(
                    getString(R.string.tv_settings_item_title_about),
                    msg = getString(R.string.tv_settings_item_msg_about),
                    dialogBtnRes = R.string.dialog_btn_ok,
                    tag = "aboutDialog"
                )
            )
        )
    }

    // dialog

    override fun onRadioDialogItemClick(checkedItem: Int, tag: String) {
        when (tag) {
            DIALOG_THEME -> {
                prefs.themeInt = checkedItem
                updateDayNight()
            }
            DIALOG_ITALIC -> prefs.emphSymbolInt = checkedItem
            DIALOG_BOLD -> prefs.strongSymbolInt = checkedItem
            DIALOG_BULLET_LIST -> prefs.bulletListSymbolInt = checkedItem
        }
        reflateViews()
    }

    override fun onTextDialogPositiveClick(input: String, tag: String) {
        when (tag) {
            DIALOG_HR -> prefs.breakSymbol = input
        }
        reflateViews()
    }

    override fun onNumberDialogPositiveClick(input: Int, tag: String?) {
        when (tag) {
            DIALOG_MAX_LINES -> prefs.notePreviewMaxLines = input
        }
        reflateViews()
    }

    override fun onUnaryDialogClick(tag: String) {}
}