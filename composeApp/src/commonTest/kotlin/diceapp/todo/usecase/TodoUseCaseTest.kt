package com.diceapp.todo.usecase

import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo
import com.diceapp.todo.repository.TodoRepository
import com.diceapp.testutil.MockTodoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TodoUseCaseTest {

    private lateinit var useCase: TodoUseCase
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        repository = MockTodoRepository()
        useCase = TodoUseCase(repository)
    }

    @Test
    fun `正常なタイトルでTodoを作成できる`() = runTest {
        val result = useCase.createTodo(
            title = "新しいタスク",
            description = "詳細な説明",
            priority = Priority.HIGH,
            category = "仕事"
        )

        assertTrue(result.isSuccess)
        result.getOrNull()?.let { todo ->
            assertEquals("新しいタスク", todo.title)
            assertEquals("詳細な説明", todo.description)
            assertEquals(Priority.HIGH, todo.priority)
            assertEquals("仕事", todo.category)
        }
    }

    @Test
    fun `空のタイトルでTodoを作成するとエラーになる`() = runTest {
        val result = useCase.createTodo(title = "")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `空白のみのタイトルでTodoを作成するとエラーになる`() = runTest {
        val result = useCase.createTodo(title = "   ")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `タイトルの前後の空白は自動的に削除される`() = runTest {
        val result = useCase.createTodo(title = "  タスク  ")
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { todo ->
            assertEquals("タスク", todo.title)
        }
    }

    @Test
    fun `存在するTodoを更新できる`() = runTest {
        val createResult = useCase.createTodo(title = "元のタスク")
        assertTrue(createResult.isSuccess)
        
        val createdTodo = createResult.getOrNull()!!
        val updateResult = useCase.updateTodo(
            id = createdTodo.id,
            title = "更新されたタスク",
            priority = Priority.HIGH
        )
        
        assertTrue(updateResult.isSuccess)
        updateResult.getOrNull()?.let { updatedTodo ->
            assertEquals("更新されたタスク", updatedTodo.title)
            assertEquals(Priority.HIGH, updatedTodo.priority)
            assertEquals(createdTodo.id, updatedTodo.id)
        }
    }

    @Test
    fun `存在しないTodoを更新するとエラーになる`() = runTest {
        val result = useCase.updateTodo(
            id = "存在しないID",
            title = "新しいタイトル"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `更新時にタイトルを空にするとエラーになる`() = runTest {
        val createResult = useCase.createTodo(title = "元のタスク")
        val createdTodo = createResult.getOrNull()!!
        
        val updateResult = useCase.updateTodo(
            id = createdTodo.id,
            title = ""
        )
        
        assertTrue(updateResult.isFailure)
        assertTrue(updateResult.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `Todoを削除できる`() = runTest {
        val createResult = useCase.createTodo(title = "削除対象")
        val createdTodo = createResult.getOrNull()!!
        
        val deleteResult = useCase.deleteTodo(createdTodo.id)
        assertTrue(deleteResult.isSuccess)
        
        val retrievedTodo = useCase.getTodoById(createdTodo.id)
        assertEquals(null, retrievedTodo)
    }

    @Test
    fun `Todo完了状態を切り替えできる`() = runTest {
        val createResult = useCase.createTodo(title = "完了切り替えテスト")
        val createdTodo = createResult.getOrNull()!!
        assertFalse(createdTodo.isCompleted)
        
        val toggleResult = useCase.toggleTodoCompletion(createdTodo.id)
        assertTrue(toggleResult.isSuccess)
        
        val retrievedTodo = useCase.getTodoById(createdTodo.id)
        assertTrue(retrievedTodo?.isCompleted ?: false)
    }

    @Test
    fun `カテゴリ別でTodoを取得できる`() = runTest {
        useCase.createTodo(title = "仕事1", category = "仕事")
        useCase.createTodo(title = "仕事2", category = "仕事")
        useCase.createTodo(title = "プライベート1", category = "プライベート")
        
        val categories = useCase.getAllCategories()
        assertTrue(categories.contains("仕事"))
        assertTrue(categories.contains("プライベート"))
    }
}