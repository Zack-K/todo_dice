package com.diceapp.todo.repository

import com.diceapp.todo.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime

class LocalTodoRepository(
    private val dataDir: String = System.getProperty("user.home") + "/.diceapp"
) : TodoRepository {

    private val todosFile = File(dataDir, "todos.json")
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())

    init {
        File(dataDir).mkdirs()
        loadTodos()
    }

    private fun loadTodos() {
        try {
            if (todosFile.exists()) {
                val jsonContent = todosFile.readText()
                if (jsonContent.isNotBlank()) {
                    val todos = json.decodeFromString<List<Todo>>(jsonContent)
                    _todos.value = todos
                }
            }
        } catch (e: Exception) {
            println("Failed to load todos: ${e.message}")
            _todos.value = emptyList()
        }
    }

    private suspend fun saveTodos() {
        try {
            val jsonContent = json.encodeToString(_todos.value)
            todosFile.writeText(jsonContent)
        } catch (e: Exception) {
            println("Failed to save todos: ${e.message}")
        }
    }

    override suspend fun getAllTodos(): Flow<List<Todo>> = _todos

    override suspend fun getTodoById(id: String): Todo? {
        return _todos.value.find { it.id == id }
    }

    override suspend fun getTodosByCategory(category: String): Flow<List<Todo>> {
        return _todos.map { todos ->
            todos.filter { it.category == category }
        }
    }

    override suspend fun insertTodo(todo: Todo): Result<Unit> {
        return try {
            val updatedTodos = _todos.value + todo
            _todos.value = updatedTodos
            saveTodos()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTodo(todo: Todo): Result<Unit> {
        return try {
            val currentTodos = _todos.value
            val index = currentTodos.indexOfFirst { it.id == todo.id }
            if (index == -1) {
                Result.failure(IllegalArgumentException("Todo not found"))
            } else {
                val updatedTodo = todo.copy(updatedAt = LocalDateTime.now().toString())
                val updatedTodos = currentTodos.toMutableList()
                updatedTodos[index] = updatedTodo
                _todos.value = updatedTodos
                saveTodos()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTodo(id: String): Result<Unit> {
        return try {
            val currentTodos = _todos.value
            val updatedTodos = currentTodos.filter { it.id != id }
            if (updatedTodos.size == currentTodos.size) {
                Result.failure(IllegalArgumentException("Todo not found"))
            } else {
                _todos.value = updatedTodos
                saveTodos()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleTodoCompletion(id: String): Result<Unit> {
        return try {
            val currentTodos = _todos.value
            val todoIndex = currentTodos.indexOfFirst { it.id == id }
            if (todoIndex == -1) {
                Result.failure(IllegalArgumentException("Todo not found"))
            } else {
                val todo = currentTodos[todoIndex]
                val updatedTodo = todo.copy(
                    isCompleted = !todo.isCompleted,
                    updatedAt = LocalDateTime.now().toString()
                )
                val updatedTodos = currentTodos.toMutableList()
                updatedTodos[todoIndex] = updatedTodo
                _todos.value = updatedTodos
                saveTodos()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllCategories(): List<String> {
        return _todos.value
            .map { it.category }
            .distinct()
            .sorted()
    }
}