package com.felwal.stratomark

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

inline fun <reified A : AppCompatActivity> AppCompatActivity.launchActivity(): Boolean {
    val intent = Intent(this, A::class.java)
    startActivity(intent)
    return true
}

fun View.snackbar(text: String, long: Boolean = true, actionText: String = "", action: ((it: View) -> Unit)? = null) =
    Snackbar
        .make(this, text, if (long) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT)
        .setAction(actionText, action)
        .show()

fun Context.toast(text: String, long: Boolean = false) =
    Toast
        .makeText(this, text, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        .show()
