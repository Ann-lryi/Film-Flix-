package com.nguonc.streamapp

import android.app.Application
import com.nguonc.streamapp.data.local.AppDatabase
import com.nguonc.streamapp.data.repository.MovieRepository

class NguonCApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MovieRepository(database.favoriteDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NguonCApplication
            private set
    }
}
