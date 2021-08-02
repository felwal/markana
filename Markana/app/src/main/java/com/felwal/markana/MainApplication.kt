package com.felwal.markana

import android.app.Application
import com.felwal.markana.prefs.Prefs

lateinit var prefs: Prefs
    private set

class MainApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()

        prefs = Prefs(applicationContext)
        appContainer = AppContainer(applicationContext)
    }
}