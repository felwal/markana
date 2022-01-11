package com.felwal.markana

import android.app.Activity
import android.app.Application
import com.felwal.markana.data.prefs.Prefs

var displayDensity: Float? = null
    private set

lateinit var prefs: Prefs
    private set

val Activity.app: App get() = application as App

class App : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        displayDensity = applicationContext.resources.displayMetrics.density
        prefs = Prefs(applicationContext)

        appContainer = AppContainer(applicationContext)
    }
}