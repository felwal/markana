package com.felwal.markana

import android.app.Application

class MainApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)
    }
}