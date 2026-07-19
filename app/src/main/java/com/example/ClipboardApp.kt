package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ClipboardRepository

class ClipboardApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ClipboardRepository(database.clipboardDao()) }
}
