package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    viewModel: ClipboardViewModel,
    itemId: Int?,
    navController: NavController
) {
    val items by viewModel.clipboardItems.collectAsStateWithLifecycle()
    val favoriteItems by viewModel.favoriteItems.collectAsStateWithLifecycle()
    val item = remember(items, favoriteItems, itemId) {
        items.find { it.id == itemId } ?: favoriteItems.find { it.id == itemId }
    }

    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val allTags = remember(items, favoriteItems) {
        (items + favoriteItems).flatMap { it.tags.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
    }

    LaunchedEffect(item) {
        if (item != null) {
            if (content.isEmpty() && item.content.isNotEmpty()) content = item.content
            if (tags.isEmpty() && item.tags.isNotEmpty()) tags = item.tags
        }
    }
    
    val currentContent by rememberUpdatedState(content)
    val currentTags by rememberUpdatedState(tags)
    val currentItem by rememberUpdatedState(item)
    
    val saveChanges = {
        currentItem?.let {
            if (currentContent != it.content || currentTags != it.tags) {
                // only update if something actually changed and is not empty initialization
                if (currentContent.isNotEmpty() || it.content.isEmpty()) {
                    viewModel.updateItem(it, currentContent, currentTags)
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            saveChanges()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Clipboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        item?.let {
                            viewModel.copyToClipboard(content)
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                    IconButton(onClick = {
                        item?.let {
                            viewModel.deleteItem(it.id)
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = tags,
                    onValueChange = { 
                        tags = it 
                        expanded = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .onFocusChanged { state ->
                            if (!state.isFocused) saveChanges()
                        },
                    label = { Text("Tags (comma separated)") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true
                )
                
                if (allTags.isNotEmpty() && expanded) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allTags.forEach { existingTag ->
                            DropdownMenuItem(
                                text = { Text(existingTag) },
                                onClick = {
                                    val currentTags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                    if (!currentTags.contains(existingTag)) {
                                        if (currentTags.isEmpty() || (currentTags.size == 1 && currentTags[0].isEmpty())) {
                                            tags = existingTag
                                        } else {
                                            tags = "$tags, $existingTag"
                                        }
                                        saveChanges()
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged { state ->
                        if (!state.isFocused) saveChanges()
                    },
                label = { Text("Content") },
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
