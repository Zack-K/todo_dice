package com.diceapp.todo.usecase

import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo
import com.diceapp.todo.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class TodoUseCase(private val repository: TodoRepository) {

    suspend fun getAllTodos(): Flow<List<Todo>> = repository.getAllTodos()

    suspend fun getTodoById(id: String): Todo? = repository.getTodoById(id)

    suspend fun getTodosByCategory(category: String): Flow<List<Todo>> = 
        repository.getTodosByCategory(category)

    suspend fun createTodo(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        category: String = "デフォルト",
        dueDate: String? = null
    ): Result<Todo> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("タイトルは必須です"))
        }

        val todo = Todo(
            title = title.trim(),
            description = description.trim(),
            priority = priority,
            category = category.trim(),
            dueDate = dueDate
        )

        return repository.insertTodo(todo).map { todo }
    }

    suspend fun updateTodo(
        id: String,
        title: String? = null,
        description: String? = null,
        priority: Priority? = null,
        category: String? = null,
        dueDate: String? = null
    ): Result<Todo> {
        val existingTodo = repository.getTodoById(id)
            ?: return Result.failure(IllegalArgumentException("指定されたTodoが見つかりません"))

        val updatedTitle = title?.trim() ?: existingTodo.title
        if (updatedTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("タイトルは必須です"))
        }

        val updatedTodo = existingTodo.copy(
            title = updatedTitle,
            description = description?.trim() ?: existingTodo.description,
            priority = priority ?: existingTodo.priority,
            category = category?.trim() ?: existingTodo.category,
            dueDate = dueDate ?: existingTodo.dueDate,
            updatedAt = java.time.LocalDateTime.now().toString()
        )

        return repository.updateTodo(updatedTodo).map { updatedTodo }
    }

    suspend fun deleteTodo(id: String): Result<Unit> = repository.deleteTodo(id)

    suspend fun toggleTodoCompletion(id: String): Result<Unit> = 
        repository.toggleTodoCompletion(id)

    suspend fun getAllCategories(): List<String> = repository.getAllCategories()

    suspend fun getCompletedTodos(): Flow<List<Todo>> {
        return repository.getAllTodos()
    }

    suspend fun getPendingTodos(): Flow<List<Todo>> {
        return repository.getAllTodos()
    }

    suspend fun getTodosByPriority(@Suppress("UNUSED_PARAMETER") priority: Priority): Flow<List<Todo>> {
        return repository.getAllTodos()
    }
}