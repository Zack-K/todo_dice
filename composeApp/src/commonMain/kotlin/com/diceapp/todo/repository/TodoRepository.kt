package com.diceapp.todo.repository

import com.diceapp.todo.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    suspend fun getAllTodos(): Flow<List<Todo>>
    suspend fun getTodoById(id: String): Todo?
    suspend fun getTodosByCategory(category: String): Flow<List<Todo>>
    suspend fun insertTodo(todo: Todo): Result<Unit>
    suspend fun updateTodo(todo: Todo): Result<Unit>
    suspend fun deleteTodo(id: String): Result<Unit>
    suspend fun toggleTodoCompletion(id: String): Result<Unit>
    suspend fun getAllCategories(): List<String>
}