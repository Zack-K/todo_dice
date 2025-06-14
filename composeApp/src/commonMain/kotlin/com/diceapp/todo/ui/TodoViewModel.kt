package com.diceapp.todo.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo
import com.diceapp.todo.repository.LocalTodoRepository
import com.diceapp.todo.usecase.TodoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TODO管理画面のViewModelクラス
 * 
 * TODOの作成、編集、削除、完了状態の切り替えを管理し、
 * フィルタリングや検索機能を提供します。
 */
class TodoViewModel {
    private val repository = LocalTodoRepository()
    private val useCase = TodoUseCase(repository)
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _allTodos = MutableStateFlow<List<Todo>>(emptyList())
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("すべて")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    var showCreateDialog by mutableStateOf(false)
        private set
    
    var editingTodo by mutableStateOf<Todo?>(null)
        private set

    init {
        loadTodos()
        loadCategories()
        setupFiltering()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            useCase.getAllTodos().collect { todoList ->
                _allTodos.value = todoList
                applyFilters()
            }
        }
    }

    private fun setupFiltering() {
        viewModelScope.launch {
            _selectedCategory.collect {
                applyFilters()
            }
        }
        viewModelScope.launch {
            _searchQuery.collect {
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val allTodos = _allTodos.value
        val category = _selectedCategory.value
        val query = _searchQuery.value
        
        _todos.value = when {
            category == "すべて" && query.isEmpty() -> allTodos
            category == "すべて" -> allTodos.filter { 
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
            query.isEmpty() -> allTodos.filter { it.category == category }
            else -> allTodos.filter { 
                it.category == category && 
                (it.title.contains(query, ignoreCase = true) ||
                 it.description.contains(query, ignoreCase = true))
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val cats = useCase.getAllCategories()
            _categories.value = listOf("すべて") + cats
        }
    }

    fun showCreateDialog() {
        showCreateDialog = true
    }

    fun hideCreateDialog() {
        showCreateDialog = false
    }

    fun showEditDialog(todo: Todo) {
        editingTodo = todo
    }

    fun hideEditDialog() {
        editingTodo = null
    }

    fun createTodo(
        title: String,
        description: String,
        priority: Priority,
        category: String,
        dueDate: String?
    ) {
        viewModelScope.launch {
            useCase.createTodo(
                title = title,
                description = description,
                priority = priority,
                category = category,
                dueDate = dueDate
            )
            hideCreateDialog()
            loadCategories()
        }
    }

    fun updateTodo(
        id: String,
        title: String,
        description: String,
        priority: Priority,
        category: String,
        dueDate: String?
    ) {
        viewModelScope.launch {
            useCase.updateTodo(
                id = id,
                title = title,
                description = description,
                priority = priority,
                category = category,
                dueDate = dueDate
            )
            hideEditDialog()
            loadCategories()
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            useCase.deleteTodo(id)
            loadCategories()
        }
    }

    fun toggleTodoCompletion(id: String) {
        viewModelScope.launch {
            useCase.toggleTodoCompletion(id)
        }
    }

    fun updateSelectedCategory(category: String) {
        viewModelScope.launch {
            _selectedCategory.value = category
        }
    }

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
        }
    }

    fun getCompletedTodosCount(): Int = _allTodos.value.count { it.isCompleted }
    fun getPendingTodosCount(): Int = _allTodos.value.count { !it.isCompleted }
    fun getHighPriorityTodosCount(): Int = _allTodos.value.count { it.priority == Priority.HIGH && !it.isCompleted }
}