package com.felwal.stratomark.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.felwal.stratomark.R
import com.felwal.stratomark.data.model.Note
import com.felwal.stratomark.databinding.ActivityEditBinding
import com.felwal.stratomark.util.close
import com.felwal.stratomark.util.selectEnd
import com.felwal.stratomark.util.showKeyboard
import com.felwal.stratomark.util.string
import com.felwal.stratomark.util.toast

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // focus body on outside click
        binding.vEmpty.setOnClickListener {
            binding.etBody.showKeyboard()
            binding.etBody.selectEnd()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tb_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                saveNote()
                close()
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onBackPressed() {
        if (hasAnyFocus()) clearAllFocus()
        else {
            saveNote()
            super.onBackPressed()
        }
    }

    private fun hasAnyFocus(): Boolean = binding.etTitle.hasFocus() || binding.etBody.hasFocus()

    private fun clearAllFocus() {
        binding.etTitle.clearFocus()
        binding.etBody.clearFocus()
    }

    private fun saveNote(): Boolean {
        var title = binding.etTitle.string
        val body = binding.etBody.string

        val splits = title.split(".")
        val extension = if (splits.size >= 2) splits.last() else ""
        title = splits.first()

        val note = Note(title, body, extension)
        if (!note.isEmpty()) {
            toast(note.toString(), true)
        }

        return true
    }
}