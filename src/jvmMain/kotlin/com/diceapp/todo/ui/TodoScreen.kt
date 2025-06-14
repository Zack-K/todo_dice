package com.diceapp.todo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diceapp.todo.model.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = remember { TodoViewModel() }
) {
    val todos by viewModel.todos.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // ヘッダー統計
        TodoStatsHeader(viewModel)
        
        Spacer(modifier = Modifier.height(16.dp))

        // 検索・フィルタバー
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            selectedCategory = selectedCategory,
            categories = categories,
            onCategoryChange = viewModel::updateSelectedCategory,
            onCreateClick = viewModel::showCreateDialog
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TODOリスト
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todos) { todo ->
                TodoItem(
                    todo = todo,
                    onToggleComplete = { viewModel.toggleTodoCompletion(todo.id) },
                    onEdit = { viewModel.showEditDialog(todo) },
                    onDelete = { viewModel.deleteTodo(todo.id) }
                )
            }
        }
    }

    // ダイアログ
    if (viewModel.showCreateDialog) {
        TodoCreateDialog(
            onDismiss = viewModel::hideCreateDialog,
            onConfirm = viewModel::createTodo,
            categories = categories.filter { it != "すべて" }
        )
    }

    viewModel.editingTodo?.let { todo ->
        TodoEditDialog(
            todo = todo,
            onDismiss = viewModel::hideEditDialog,
            onConfirm = viewModel::updateTodo,
            categories = categories.filter { it != "すべて" }
        )
    }
}

@Composable
private fun TodoStatsHeader(viewModel: TodoViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("完了", viewModel.getCompletedTodosCount().toString(), Icons.Default.CheckCircle)
            StatItem("未完了", viewModel.getPendingTodosCount().toString(), Icons.Default.RadioButtonUnchecked)
            StatItem("重要", viewModel.getHighPriorityTodosCount().toString(), Icons.Default.PriorityHigh)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 検索フィールド
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("タスクを検索...") },
            leadingIcon = { Icon(Icons.Default.Search, "検索") },
            modifier = Modifier.weight(1f)
        )

        // カテゴリフィルタ
        var categoryDropdownExpanded by remember { mutableStateOf(false) }
        
        Box {
            OutlinedButton(
                onClick = { categoryDropdownExpanded = true }
            ) {
                Text(selectedCategory)
                Icon(Icons.Default.ArrowDropDown, "カテゴリ選択")
            }
            
            DropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onCategoryChange(category)
                            categoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // 新規作成ボタン
        FloatingActionButton(
            onClick = onCreateClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Add, "新規作成")
        }
    }
}