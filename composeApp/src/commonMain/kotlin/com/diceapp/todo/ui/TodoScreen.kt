package com.diceapp.todo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    
    // サンプルデータ
    val sampleTodos = remember {
        mutableStateListOf(
            Todo(
                title = "北海道旅行の計画を立てる",
                description = "ルートと宿泊先を決める",
                category = "旅行",
                priority = Priority.HIGH,
                isCompleted = false
            ),
            Todo(
                title = "お土産リストを作成",
                description = "家族と友人用",
                category = "旅行", 
                priority = Priority.MEDIUM,
                isCompleted = false
            ),
            Todo(
                title = "カメラの充電器を用意",
                description = "",
                category = "準備",
                priority = Priority.LOW,
                isCompleted = true
            )
        )
    }
    
    val filteredTodos = if (searchQuery.isEmpty()) {
        sampleTodos
    } else {
        sampleTodos.filter { 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダーカード
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📝 TODO管理",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "タスクを整理して効率的に",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 検索バー - モバイル最適化
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("TODOを検索") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "検索",
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { 
                            searchQuery = ""
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "クリア",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 統計情報カード
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${sampleTodos.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "総数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${sampleTodos.count { !it.isCompleted }}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "未完了",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${sampleTodos.count { it.isCompleted }}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "完了",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TODOリスト
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTodos) { todo ->
                TodoCard(
                    todo = todo,
                    onToggleComplete = { todoId ->
                        val index = sampleTodos.indexOfFirst { it.id == todoId }
                        if (index != -1) {
                            sampleTodos[index] = sampleTodos[index].copy(
                                isCompleted = !sampleTodos[index].isCompleted
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                )
            }
            
            if (filteredTodos.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔍",
                                    fontSize = 32.sp
                                )
                                Text(
                                    text = if (searchQuery.isEmpty()) "TODOがありません" else "該当するTODOがありません",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoCard(
    todo: Todo,
    onToggleComplete: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (todo.isCompleted) 1.dp else 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                todo.isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                todo.priority == Priority.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                todo.priority == Priority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // チェックボックス - モバイル最適化
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { 
                    onToggleComplete(todo.id)
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                    color = if (todo.isCompleted) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (todo.description.isNotEmpty()) {
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // カテゴリタグ
                    AssistChip(
                        onClick = { /* TODO: カテゴリフィルタ */ },
                        label = { 
                            Text(
                                text = todo.category,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        modifier = Modifier.height(24.dp)
                    )
                    
                    // 優先度タグ
                    val priorityColor = when (todo.priority) {
                        Priority.HIGH -> MaterialTheme.colorScheme.error
                        Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                        Priority.LOW -> MaterialTheme.colorScheme.tertiary
                    }
                    
                    AssistChip(
                        onClick = { /* TODO: 優先度フィルタ */ },
                        label = { 
                            Text(
                                text = when (todo.priority) {
                                    Priority.HIGH -> "高"
                                    Priority.MEDIUM -> "中" 
                                    Priority.LOW -> "低"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}