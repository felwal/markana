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
import com.felwal.markana.util.then
import com.felwal.markana.util.updateTheme
import com.felwal.markana.widget.dialog.RadioDialog
import com.felwal.markana.widget.dialog.TextDialog
import com.felwal.markana.widget.dialog.UnaryDialog

private const val DIALOG_THEME = "themeDialog"
private const val DIALOG_ITALIC = "italicDialog"
private const val DIALOG_BOLD = "boldDialog"
private const val DIALOG_BULLETLIST = "bulletlistDialog"
private const val DIALOG_HR = "hrDialog"

open class SettingsActivity : AbsSettingsActivity(
    dividerMode = DividerMode.IN_SECTION,
    indentEverything = false
),
    RadioDialog.DialogListener,
    TextDialog.DialogListener,
    UnaryDialog.DialogListener
{

    private lateinit var binding: ActivitySettingsBinding

    override val llItemContainer: LinearLayout get() = binding.ll

    override fun onCreate(savedInstanceState: Bundle?) {
        updateTheme()
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init tb
        setSupportActionBar(binding.tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // animate tb elevation on scroll
        binding.nsv.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.ab.isSelected = binding.nsv.canScrollVertically(-1)
        }

        inflateViews()
    }

    // Activity

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> finish() then true

            else -> super.onOptionsItemSelected(item)
        }

    //

    override fun inflateViews() {
        inflateSections(
            ItemSection(
                title = getString(R.string.tv_settings_header_appearance),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_theme),
                    values = Theme.values().map { it.title },
                    selectedIndex = prefs.themeInt,
                    iconRes = R.drawable.ic_theme,
                    tag = DIALOG_THEME
                ),
            ),
            ItemSection(
                title = getString(R.string.tv_settings_header_markdown),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_italic_symbol),
                    values = Emph.values().map { it.title },
                    selectedIndex = prefs.emphSymbolInt,
                    iconRes = R.drawable.ic_italic,
                    tag = DIALOG_ITALIC
                ),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_bold_symbol),
                    values = Strong.values().map { it.title },
                    selectedIndex = prefs.strongSymbolInt,
                    iconRes = R.drawable.ic_bold,
                    tag = DIALOG_BOLD
                ),
                SingleSelectionItem(
                    title = getString(R.string.tv_settings_item_title_bulletlist_symbol),
                    values = Bullet.values().map { it.title },
                    selectedIndex = prefs.bulletlistSymbolInt,
                    iconRes = R.drawable.ic_list_bullet,
                    tag = DIALOG_BULLETLIST
                ),
                StringItem(
                    title = getString(R.string.tv_settings_item_title_hr_symbol),
                    value = prefs.breakSymbol,
                    hint = "3 or more of *, -, or _",
                    iconRes = R.drawable.ic_horizontal_rule,
                    tag = DIALOG_HR
                ),
                BooleanItem(
                    title = getString(R.string.tv_settings_item_title_checkbox_space),
                    value = prefs.checkboxSpace,
                    iconRes = R.drawable.ic_checkbox_blank,
                    onSwitch = { prefs.checkboxSpace = !prefs.checkboxSpace }
                ),
            ),
            ItemSection(
                title = "About and other",
                InfoItem(
                    getString(R.string.tv_settings_item_title_about),
                    desc = getString(R.string.tv_settings_item_msg_about),
                    dialogBtnRes = R.string.dialog_btn_ok,
                    tag = "aboutDialog"
                )
            )
        )
    }

    // dialog

    override fun onRadioDialogClick(index: Int, tag: String) {
        when (tag) {
            DIALOG_THEME -> {
                prefs.themeInt = index
                updateTheme()
            }
            DIALOG_ITALIC -> prefs.emphSymbolInt = index
            DIALOG_BOLD -> prefs.strongSymbolInt = index
            DIALOG_BULLETLIST -> prefs.bulletlistSymbolInt = index
        }
        reflateViews()
    }

    override fun onTextDialogPositiveClick(input: String, tag: String) {
        when (tag) {
            DIALOG_HR -> prefs.breakSymbol = input
        }
        reflateViews()
    }

    override fun onUnaryDialogClick(tag: String) {}
}