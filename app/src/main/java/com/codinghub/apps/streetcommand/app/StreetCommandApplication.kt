package com.codinghub.apps.streetcommand.app

import android.app.Application
import android.content.Context

class StreetCommandApplication : Application() {

    companion object {
        lateinit var instance : StreetCommandApplication

        fun getAppContext() : Context = instance.applicationContext
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}