package com.felwal.markana.ui.setting

import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import com.felwal.markana.R
import com.felwal.markana.databinding.ActivitySettingsBinding
import com.felwal.markana.widget.dialog.RadioDialog
import com.felwal.markana.widget.dialog.TextDialog
import com.felwal.markana.widget.dialog.UnaryDialog
import com.felwal.markana.prefs
import com.felwal.markana.data.prefs.bulletlistSymbolNames
import com.felwal.markana.data.prefs.emphSymbolNames
import com.felwal.markana.data.prefs.themeNames
import com.felwal.markana.util.then

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

    override val ll: LinearLayout get() = binding.ll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            ItemSection(getString(R.string.tv_settings_header_appearance),
                SingleSelectionItem(getString(R.string.tv_settings_item_title_theme),
                    values = themeNames,
                    selectedIndex = prefs.themeInt,
                    iconRes = R.drawable.ic_theme,
                    tag = DIALOG_THEME
                ),
            ),
            ItemSection(getString(R.string.tv_settings_header_markdown),
                SingleSelectionItem(getString(R.string.tv_settings_item_title_italic_symbol),
                    values = emphSymbolNames,
                    selectedIndex = prefs.italicSymbolInt,
                    iconRes = R.drawable.ic_italic,
                    tag = DIALOG_ITALIC
                ),
                SingleSelectionItem(getString(R.string.tv_settings_item_title_bold_symbol),
                    values = emphSymbolNames,
                    selectedIndex = prefs.boldSymbolInt,
                    iconRes = R.drawable.ic_bold,
                    tag = DIALOG_BOLD
                ),
                SingleSelectionItem(getString(R.string.tv_settings_item_title_bulletlist_symbol),
                    values = bulletlistSymbolNames,
                    selectedIndex = prefs.bulletlistSymbolInt,
                    iconRes = R.drawable.ic_list_bullet,
                    tag = DIALOG_BULLETLIST
                ),
                StringItem(getString(R.string.tv_settings_item_title_hr_symbol),
                    value = prefs.hrSymbol,
                    hint = "3 or more of *, -, or _",
                    iconRes = R.drawable.ic_horizontal_rule,
                    tag = DIALOG_HR
                ),
                BooleanItem(getString(R.string.tv_settings_item_title_checkbox_space),
                    value = prefs.checkboxSpace,
                    iconRes = R.drawable.ic_checkbox_blank,
                    onSwitch = { prefs.checkboxSpace = !prefs.checkboxSpace }
                ),
            ),
            ItemSection("About and other",
                InfoItem(getString(R.string.tv_settings_item_title_about),
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
                recreate()
            }
            DIALOG_ITALIC -> prefs.italicSymbolInt = index
            DIALOG_BOLD -> prefs.boldSymbolInt = index
            DIALOG_BULLETLIST -> prefs.bulletlistSymbolInt = index
        }
        reflateViews()
    }

    override fun onTextDialogPositiveClick(input: String, tag: String) {
        when (tag) {
            DIALOG_HR -> prefs.hrSymbol = input
        }
        reflateViews()
    }

    override fun onUnaryDialogClick(passValue: String?, tag: String) {}
}