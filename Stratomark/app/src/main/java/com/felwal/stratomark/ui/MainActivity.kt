package com.felwal.stratomark.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.felwal.stratomark.R

import com.felwal.stratomark.databinding.ActivityMainBinding
import com.felwal.stratomark.util.launchActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tb)

        initFab()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tb_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_settings -> launchActivity<SettingsActivity>()
            else -> super.onOptionsItemSelected(item)
        }

    private fun initFab() {
        binding.fab.setOnClickListener { view ->
            launchActivity<EditActivity>()
            //view.snackbar("Replace with your own action", actionText = "Action") {}
        }
    }
}