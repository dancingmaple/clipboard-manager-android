package com.example

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.service.ClipboardService
import com.example.service.FloatingBubbleService
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.SettingsManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        startMonitorServiceIfNeeded()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        checkClipboard()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            checkClipboard()
        }
    }
    
    private fun startMonitorServiceIfNeeded() {
        val settingsManager = SettingsManager(this)
        if (settingsManager.isBootEnabled.value) {
            val serviceIntent = Intent(this, ClipboardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
        if (settingsManager.isFloatingBubbleEnabled.value) {
            if (android.provider.Settings.canDrawOverlays(this)) {
                val serviceIntent = Intent(this, FloatingBubbleService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            }
        }
    }

    private fun checkClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank()) {
                    val app = application as ClipboardApp
                    val settingsManager = SettingsManager(this)
                    lifecycleScope.launch {
                        app.repository.insertOrUpdate(
                            content = text,
                            isAutoDeduplicate = settingsManager.isAutoDeduplicate.value,
                            maxItems = settingsManager.maxItems.value
                        )
                    }
                }
            }
        }
    }
}
