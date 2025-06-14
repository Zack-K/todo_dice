package com.diceapp.todo.repository

import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TodoRepositoryTest {

    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        repository = MockTodoRepository()
    }

    @Test
    fun `新しいTodoを挿入できる`() = runBlocking {
        val todo = Todo(title = "新しいタスク")
        val result = repository.insertTodo(todo)
        
        assertTrue(result.isSuccess)
        
        val retrievedTodo = repository.getTodoById(todo.id)
        assertNotNull(retrievedTodo)
        assertEquals(todo.title, retrievedTodo.title)
    }

    @Test
    fun `Todoを更新できる`() = runBlocking {
        val todo = Todo(title = "元のタスク")
        repository.insertTodo(todo)
        
        val updatedTodo = todo.copy(title = "更新されたタスク", isCompleted = true)
        val result = repository.updateTodo(updatedTodo)
        
        assertTrue(result.isSuccess)
        
        val retrievedTodo = repository.getTodoById(todo.id)
        assertNotNull(retrievedTodo)
        assertEquals("更新されたタスク", retrievedTodo.title)
        assertTrue(retrievedTodo.isCompleted)
    }

    @Test
    fun `Todoを削除できる`() = runBlocking {
        val todo = Todo(title = "削除対象のタスク")
        repository.insertTodo(todo)
        
        val result = repository.deleteTodo(todo.id)
        assertTrue(result.isSuccess)
        
        val retrievedTodo = repository.getTodoById(todo.id)
        assertNull(retrievedTodo)
    }

    @Test
    fun `Todo完了状態を切り替えできる`() = runBlocking {
        val todo = Todo(title = "完了切り替えテスト", isCompleted = false)
        repository.insertTodo(todo)
        
        val result = repository.toggleTodoCompletion(todo.id)
        assertTrue(result.isSuccess)
        
        val retrievedTodo = repository.getTodoById(todo.id)
        assertNotNull(retrievedTodo)
        assertTrue(retrievedTodo.isCompleted)
    }

    @Test
    fun `カテゴリ別でTodoを取得できる`() = runBlocking {
        val todo1 = Todo(title = "仕事タスク1", category = "仕事")
        val todo2 = Todo(title = "仕事タスク2", category = "仕事")
        val todo3 = Todo(title = "プライベートタスク", category = "プライベート")
        
        repository.insertTodo(todo1)
        repository.insertTodo(todo2)
        repository.insertTodo(todo3)
        
        val workTodos = repository.getTodosByCategory("仕事").first()
        assertEquals(2, workTodos.size)
        assertTrue(workTodos.all { it.category == "仕事" })
    }

    @Test
    fun `全てのカテゴリを取得できる`() = runBlocking {
        val todo1 = Todo(title = "タスク1", category = "仕事")
        val todo2 = Todo(title = "タスク2", category = "プライベート")
        val todo3 = Todo(title = "タスク3", category = "買い物")
        
        repository.insertTodo(todo1)
        repository.insertTodo(todo2)
        repository.insertTodo(todo3)
        
        val categories = repository.getAllCategories()
        assertTrue(categories.contains("仕事"))
        assertTrue(categories.contains("プライベート"))
        assertTrue(categories.contains("買い物"))
    }
}

// テスト用のMockRepository
class MockTodoRepository : TodoRepository {
    private val todos = mutableMapOf<String, Todo>()

    override suspend fun getAllTodos(): Flow<List<Todo>> {
        return flowOf(todos.values.toList())
    }

    override suspend fun getTodoById(id: String): Todo? {
        return todos[id]
    }

    override suspend fun getTodosByCategory(category: String): Flow<List<Todo>> {
        return flowOf(todos.values.filter { it.category == category })
    }

    override suspend fun insertTodo(todo: Todo): Result<Unit> {
        todos[todo.id] = todo
        return Result.success(Unit)
    }

    override suspend fun updateTodo(todo: Todo): Result<Unit> {
        if (todos.containsKey(todo.id)) {
            todos[todo.id] = todo
            return Result.success(Unit)
        }
        return Result.failure(Exception("Todo not found"))
    }

    override suspend fun deleteTodo(id: String): Result<Unit> {
        if (todos.containsKey(id)) {
            todos.remove(id)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Todo not found"))
    }

    override suspend fun toggleTodoCompletion(id: String): Result<Unit> {
        val todo = todos[id]
        if (todo != null) {
            todos[id] = todo.copy(isCompleted = !todo.isCompleted)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Todo not found"))
    }

    override suspend fun getAllCategories(): List<String> {
        return todos.values.map { it.category }.distinct()
    }
}