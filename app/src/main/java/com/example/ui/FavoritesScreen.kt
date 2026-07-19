package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.components.ClipboardItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: ClipboardViewModel,
    navController: NavController
) {
    val items by viewModel.favoriteItems.collectAsStateWithLifecycle()
    var selectedTag by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var showExportDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showBulkTagDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val selectedItemIds = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateListOf<Int>() }
    val isSelectionMode = selectedItemIds.isNotEmpty()

    val allTags = androidx.compose.runtime.remember(items) {
        items.flatMap { it.tags.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
    }

    val filteredItems = androidx.compose.runtime.remember(items, selectedTag) {
        if (selectedTag == null) items else items.filter { it.tags.split(",").map { t -> t.trim() }.contains(selectedTag) }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { showBulkTagDialog = true }) {
                        Icon(Icons.Default.Label, contentDescription = "Tag")
                    }
                    IconButton(onClick = { 
                        selectedItemIds.forEach { id -> 
                            items.find { it.id == id }?.let { item ->
                                if (item.isFavorite) viewModel.toggleFavorite(item)
                            }
                        }
                        selectedItemIds.clear()
                    }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Unfavorite")
                    }
                    IconButton(onClick = { 
                        showExportDialog = true
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(onClick = { 
                        selectedItemIds.forEach { viewModel.deleteItem(it) }
                        selectedItemIds.clear()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedItemIds.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                        Text(
                            text = "${selectedItemIds.size} Selected",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            if (selectedItemIds.size == filteredItems.size) {
                                selectedItemIds.clear()
                            } else {
                                selectedItemIds.clear()
                                selectedItemIds.addAll(filteredItems.map { it.id })
                            }
                        }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Select All")
                        }
                    } else {
                        Text(
                            text = "Favorites",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                if (!isSelectionMode && allTags.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            androidx.compose.material3.FilterChip(
                                selected = selectedTag == null,
                                onClick = { selectedTag = null },
                                label = { Text("All") }
                            )
                        }
                        items(allTags, key = { it }) { tag ->
                            androidx.compose.material3.FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = if (selectedTag == tag) null else tag },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                Text(
                    text = "No favorite items",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isSelected = selectedItemIds.contains(item.id)
                        ClipboardItemCard(
                            item = item,
                            selected = isSelected,
                            onLongClick = {
                                if (isSelected) selectedItemIds.remove(item.id) else selectedItemIds.add(item.id)
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedItemIds.remove(item.id) else selectedItemIds.add(item.id)
                                } else {
                                    navController.navigate("edit/${item.id}")
                                }
                            },
                            onCopy = { viewModel.copyToClipboard(item.content) },
                            onDelete = { viewModel.deleteItem(item.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) }
                        )
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        val itemsToExport = if (isSelectionMode) filteredItems.filter { selectedItemIds.contains(it.id) } else filteredItems
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Format") },
            text = { Text("Selected items will be saved to your device.") },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = { viewModel.exportData("txt", itemsToExport); showExportDialog = false; selectedItemIds.clear() }) { Text("Export as TXT") }
                    TextButton(onClick = { viewModel.exportData("csv", itemsToExport); showExportDialog = false; selectedItemIds.clear() }) { Text("Export as CSV") }
                    TextButton(onClick = { viewModel.exportData("json", itemsToExport); showExportDialog = false; selectedItemIds.clear() }) { Text("Export as JSON") }
                }
            }
        )
    }
    
    if (showBulkTagDialog) {
        var bulkTags by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBulkTagDialog = false },
            title = { Text("Add Tags") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = bulkTags,
                    onValueChange = { bulkTags = it },
                    label = { Text("Tags (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedItemIds.forEach { id ->
                        items.find { it.id == id }?.let { item ->
                            val currentTags = item.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                            currentTags.addAll(bulkTags.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                            viewModel.updateItem(item, item.content, currentTags.joinToString(", "))
                        }
                    }
                    showBulkTagDialog = false
                    selectedItemIds.clear()
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
