package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.ClipboardApp
import com.example.util.SettingsManager
import kotlinx.coroutines.launch

class SaveClipboardActivity : ComponentActivity() {
    private var handled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Delay to handle case where focus isn't gained immediately
        window.decorView.postDelayed({
            if (!handled) {
                handled = true
                saveClipboardAndFinish()
            }
        }, 500)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !handled) {
            handled = true
            saveClipboardAndFinish()
        }
    }

    private fun saveClipboardAndFinish() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(this).toString()
            if (text.isNotBlank()) {
                val app = application as ClipboardApp
                val settingsManager = SettingsManager(this)
                lifecycleScope.launch {
                    app.repository.insertOrUpdate(
                        content = text,
                        isAutoDeduplicate = settingsManager.isAutoDeduplicate.value,
                        maxItems = settingsManager.maxItems.value
                    )
                    Toast.makeText(this@SaveClipboardActivity, "Saved to Clipboard", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return
            }
        }
        
        Toast.makeText(this, "Failed to read or clipboard is empty", Toast.LENGTH_SHORT).show()
        finish()
    }
}
