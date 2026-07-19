package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ClipboardApp
import com.example.data.ClipboardItem
import com.example.util.SettingsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClipboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ClipboardApp).repository
    val settingsManager = SettingsManager(application)
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val clipboardItems: StateFlow<List<ClipboardItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allItems else repository.searchItems(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteItems: StateFlow<List<ClipboardItem>> = repository.favoriteItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun copyToClipboard(content: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Clipboard Manager", content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "已复制到剪切板", Toast.LENGTH_SHORT).show()
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteItemById(id)
        }
    }

    fun deleteItems(ids: List<Int>) {
        viewModelScope.launch {
            repository.deleteItemsByIds(ids)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun toggleFavorite(item: ClipboardItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun updateItem(item: ClipboardItem, newContent: String, newTags: String) {
        viewModelScope.launch {
            repository.updateItem(item.copy(content = newContent, tags = newTags))
        }
    }

    fun exportData(format: String, itemsToExport: List<ClipboardItem>? = null) {
        viewModelScope.launch {
            val items = itemsToExport ?: clipboardItems.value
            if (items.isEmpty()) {
                Toast.makeText(getApplication(), "没有可导出的数据", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val context = getApplication<Application>()
            val dir = File(context.cacheDir, "exports")
            if (!dir.exists()) dir.mkdirs()
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(dir, "clipboard_export_$timestamp.$format")

            try {
                FileWriter(file).use { writer ->
                    when (format) {
                        "txt" -> {
                            items.forEach {
                                writer.write(it.content)
                                writer.write("\n======================\n")
                            }
                        }
                        "csv" -> {
                            writer.write("ID,Content,CreatedTime,Tags\n")
                            items.forEach {
                                val escapedContent = it.content.replace("\"", "\"\"")
                                writer.write("${it.id},\"$escapedContent\",${it.createdTime},\"${it.tags}\"\n")
                            }
                        }
                        "json" -> {
                            val jsonArray = JSONArray()
                            items.forEach {
                                val obj = JSONObject().apply {
                                    put("id", it.id)
                                    put("content", it.content)
                                    put("createdTime", it.createdTime)
                                    put("isFavorite", it.isFavorite)
                                    put("tags", it.tags)
                                }
                                jsonArray.put(obj)
                            }
                            writer.write(jsonArray.toString(2))
                        }
                    }
                }

                shareFile(file)
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareFile(file: File) {
        val context = getApplication<Application>()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "导出数据")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
