package com.diceapp.todo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoCreateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Priority, String, String?) -> Unit,
    categories: List<String>
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var category by remember { mutableStateOf("デフォルト") }
    var dueDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新しいタスクを作成") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // タイトル
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank()
                )

                // 説明
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("説明") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // 優先度選択
                PrioritySelector(
                    selectedPriority = priority,
                    onPriorityChange = { priority = it }
                )

                // カテゴリ選択
                CategorySelector(
                    selectedCategory = category,
                    categories = categories,
                    onCategoryChange = { category = it }
                )

                // 期日
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("期日 (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例: 2024-12-31") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            priority,
                            category.trim(),
                            if (dueDate.isBlank()) null else dueDate.trim()
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("作成")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditDialog(
    todo: Todo,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Priority, String, String?) -> Unit,
    categories: List<String>
) {
    var title by remember { mutableStateOf(todo.title) }
    var description by remember { mutableStateOf(todo.description) }
    var priority by remember { mutableStateOf(todo.priority) }
    var category by remember { mutableStateOf(todo.category) }
    var dueDate by remember { mutableStateOf(todo.dueDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("タスクを編集") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // タイトル
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank()
                )

                // 説明
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("説明") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // 優先度選択
                PrioritySelector(
                    selectedPriority = priority,
                    onPriorityChange = { priority = it }
                )

                // カテゴリ選択
                CategorySelector(
                    selectedCategory = category,
                    categories = categories,
                    onCategoryChange = { category = it }
                )

                // 期日
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("期日 (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例: 2024-12-31") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            todo.id,
                            title.trim(),
                            description.trim(),
                            priority,
                            category.trim(),
                            if (dueDate.isBlank()) null else dueDate.trim()
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("保存")
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
private fun PrioritySelector(
    selectedPriority: Priority,
    onPriorityChange: (Priority) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "優先度",
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
                    Text(
                        text = when (selectedPriority) {
                            Priority.HIGH -> "高"
                            Priority.MEDIUM -> "中"
                            Priority.LOW -> "低"
                        }
                    )
                    Icon(Icons.Default.ArrowDropDown, "優先度選択")
                }
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Priority.values().forEach { priority ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                when (priority) {
                                    Priority.HIGH -> "高"
                                    Priority.MEDIUM -> "中"
                                    Priority.LOW -> "低"
                                }
                            ) 
                        },
                        onClick = {
                            onPriorityChange(priority)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }
    var showNewCategoryField by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "カテゴリ",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        if (showNewCategoryField) {
            OutlinedTextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                label = { Text("新しいカテゴリ") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row {
                        IconButton(
                            onClick = {
                                if (newCategory.isNotBlank()) {
                                    onCategoryChange(newCategory.trim())
                                    showNewCategoryField = false
                                    newCategory = ""
                                }
                            }
                        ) {
                            Text("✓")
                        }
                        IconButton(
                            onClick = {
                                showNewCategoryField = false
                                newCategory = ""
                            }
                        ) {
                            Text("✗")
                        }
                    }
                }
            )
        } else {
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
                    
                    Divider()
                    
                    DropdownMenuItem(
                        text = { Text("+ 新しいカテゴリ") },
                        onClick = {
                            showNewCategoryField = true
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}