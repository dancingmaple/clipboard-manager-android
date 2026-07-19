package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.components.ClipboardItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    viewModel: ClipboardViewModel,
    navController: NavController
) {
    val items by viewModel.clipboardItems.collectAsStateWithLifecycle()
    var selectedTag by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    
    val allTags = remember(items) {
        items.flatMap { it.tags.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
    }
    
    // Automatically select first tag if null
    LaunchedEffect(allTags) {
        if (selectedTag == null && allTags.isNotEmpty()) {
            selectedTag = allTags.first()
        } else if (!allTags.contains(selectedTag)) {
            selectedTag = allTags.firstOrNull()
        }
    }

    val filteredItems = remember(items, selectedTag, searchQuery) {
        val tagged = if (selectedTag == null) items else items.filter { it.tags.split(",").map { t -> t.trim() }.contains(selectedTag) }
        if (searchQuery.isEmpty()) tagged else tagged.filter { it.content.contains(searchQuery, ignoreCase = true) || it.tags.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search tags or content...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            if (allTags.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tags found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left side: Tags
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.3f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        items(allTags, key = { it }) { tag ->
                            val isSelected = selectedTag == tag
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTag = tag }
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .padding(vertical = 16.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (tag.length > 3) tag.take(3) + ".." else tag,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Right side: Items
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            ClipboardItemCard(
                                item = item,
                                onClick = { navController.navigate("edit/${item.id}") },
                                onCopy = { viewModel.copyToClipboard(item.content) },
                                onDelete = { viewModel.deleteItem(item.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(item) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
