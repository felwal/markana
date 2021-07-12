package com.felwal.stratomark.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.felwal.stratomark.R

import com.felwal.stratomark.databinding.ActivityMainBinding
import com.felwal.stratomark.launchActivity
import com.felwal.stratomark.snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        initFab()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> launchActivity<SettingsActivity>()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initFab() {
        binding.fab.setOnClickListener { view ->
            view.snackbar("Replace with your own action", actionText = "Action") {}
        }
    }
}