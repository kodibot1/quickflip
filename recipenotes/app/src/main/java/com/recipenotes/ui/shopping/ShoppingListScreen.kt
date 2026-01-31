package com.recipenotes.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipenotes.ui.shopping.components.AddItemDialog
import com.recipenotes.ui.shopping.components.ShoppingItemRow

/**
 * Shopping list screen with auto-generation from meal plan and manual item management.
 *
 * UX Design:
 * - Unchecked items appear first (things you still need to buy)
 * - Checked items appear below with strikethrough (things already in your cart)
 * - This natural grouping helps users scan the list quickly at the store
 *
 * @param generateFromWeek If non-null, triggers shopping list generation from that week's meal plan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    generateFromWeek: String? = null,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // If we navigated here from meal plan with a week to generate from
    LaunchedEffect(generateFromWeek) {
        generateFromWeek?.let { viewModel.generateFromMealPlan(it) }
    }

    val uncheckedItems = items.filter { !it.isChecked }
    val checkedItems = items.filter { it.isChecked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                actions = {
                    // Clear checked items
                    if (checkedItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearChecked() }) {
                            Icon(Icons.Filled.PlaylistRemove, contentDescription = "Clear checked items")
                        }
                    }
                    // Clear all items
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(Icons.Filled.ClearAll, contentDescription = "Clear all items")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add item")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No items yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Generate from meal plan or add items manually",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Unchecked items
                items(uncheckedItems, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = { viewModel.toggleItem(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }

                // Divider between unchecked and checked
                if (uncheckedItems.isNotEmpty() && checkedItems.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            HorizontalDivider()
                            Text(
                                "Checked (${checkedItems.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Checked items
                items(checkedItems, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = { viewModel.toggleItem(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }
    }

    // Add item dialog
    if (showAddDialog) {
        AddItemDialog(
            onAdd = { name, qty, unit -> viewModel.addManualItem(name, qty, unit) },
            onDismiss = { showAddDialog = false }
        )
    }
}
