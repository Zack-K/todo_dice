package com.diceapp.randomselector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diceapp.randomselector.model.SelectionResult
import com.diceapp.randomselector.repository.SelectionStatistics
import com.diceapp.todo.model.Todo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeSelector(
    currentMode: RandomSelectorViewModel.SelectionMode,
    onModeChange: (RandomSelectorViewModel.SelectionMode) -> Unit
) {
    Column {
        Text(
            text = "選択方法",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RandomSelectorViewModel.SelectionMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentMode == mode,
                        onClick = { onModeChange(mode) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentMode == mode,
                    onClick = { onModeChange(mode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (mode) {
                        RandomSelectorViewModel.SelectionMode.FROM_CATEGORY -> "カテゴリから選択"
                        RandomSelectorViewModel.SelectionMode.FROM_SELECTED_TODOS -> "指定TODOから選択"
                        RandomSelectorViewModel.SelectionMode.CUSTOM -> "カスタム選択肢"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "カテゴリ",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCategory)
                    Icon(Icons.Default.ArrowDropDown, "カテゴリ選択")
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onCategoryChange(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TodoSelector(
    todos: List<Todo>,
    onSelectionPerformed: (List<String>) -> Unit
) {
    var selectedTodos by remember { mutableStateOf(setOf<String>()) }

    Column {
        Text(
            text = "TODO選択（最大6個）",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (todos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未完了のTODOがありません",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(todos.take(20)) { todo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedTodos.contains(todo.id),
                            onCheckedChange = { checked ->
                                selectedTodos = if (checked && selectedTodos.size < 6) {
                                    selectedTodos + todo.id
                                } else {
                                    selectedTodos - todo.id
                                }
                            },
                            enabled = selectedTodos.contains(todo.id) || selectedTodos.size < 6
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Button(
                onClick = { onSelectionPerformed(selectedTodos.toList()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTodos.isNotEmpty()
            ) {
                Icon(Icons.Default.Shuffle, "実行")
                Spacer(modifier = Modifier.width(8.dp))
                Text("選択されたTODOから実行 (${selectedTodos.size}個)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionResultDisplay(result: SelectionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 選択情報
            Text(
                text = result.selection.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ダイス結果
            Text(
                text = "🎲 ${result.diceRoll}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 選択された項目（大きく表示）
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = result.selectedItem.text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 他の選択肢表示
            if (result.selection.items.size > 1) {
                Text(
                    text = "他の選択肢:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                result.selection.items
                    .filter { it.id != result.selectedItem.id }
                    .forEach { item ->
                        Text(
                            text = "• ${item.text}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionHistoryItem(result: SelectionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 結果詳細
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.selectedItem.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${result.selection.title} (${result.diceRoll}番)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.timestamp.substringAfter('T').substringBefore('.'),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ダイス結果
            Text(
                text = "🎲${result.diceRoll}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CustomInputDialog(
    customTexts: List<String>,
    onTextChange: (Int, String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("カスタム選択肢を入力") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customTexts.size) { index ->
                    OutlinedTextField(
                        value = customTexts[index],
                        onValueChange = { onTextChange(index, it) },
                        label = { Text("選択肢 ${index + 1}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = customTexts.any { it.isNotBlank() }
            ) {
                Text("実行")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun SelectionStatisticsDialog(
    onDismiss: () -> Unit,
    getStatistics: suspend () -> SelectionStatistics
) {
    var statistics by remember { mutableStateOf<SelectionStatistics?>(null) }

    LaunchedEffect(Unit) {
        statistics = getStatistics()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("選択統計") },
        text = {
            statistics?.let { stats ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow("総選択回数", stats.totalSelections.toString())
                    
                    if (stats.mostUsedItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "よく選ばれる項目",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        stats.mostUsedItems.toList()
                            .sortedByDescending { it.second }
                            .take(10)
                            .forEach { (item, count) ->
                                StatRow(item, "$count 回")
                            }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}