package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _isAutoDeduplicate = MutableStateFlow(prefs.getBoolean("auto_deduplicate", true))
    val isAutoDeduplicate: StateFlow<Boolean> = _isAutoDeduplicate.asStateFlow()

    private val _maxItems = MutableStateFlow(prefs.getInt("max_items", 1000))
    val maxItems: StateFlow<Int> = _maxItems.asStateFlow()
    
    private val _isBootEnabled = MutableStateFlow(prefs.getBoolean("boot_enabled", false))
    val isBootEnabled: StateFlow<Boolean> = _isBootEnabled.asStateFlow()

    private val _isFloatingBubbleEnabled = MutableStateFlow(prefs.getBoolean("floating_bubble", false))
    val isFloatingBubbleEnabled: StateFlow<Boolean> = _isFloatingBubbleEnabled.asStateFlow()

    fun setAutoDeduplicate(enabled: Boolean) {
        prefs.edit().putBoolean("auto_deduplicate", enabled).apply()
        _isAutoDeduplicate.value = enabled
    }

    fun setMaxItems(count: Int) {
        prefs.edit().putInt("max_items", count).apply()
        _maxItems.value = count
    }
    
    fun setBootEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("boot_enabled", enabled).apply()
        _isBootEnabled.value = enabled
    }

    fun setFloatingBubbleEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("floating_bubble", enabled).apply()
        _isFloatingBubbleEnabled.value = enabled
    }
}
