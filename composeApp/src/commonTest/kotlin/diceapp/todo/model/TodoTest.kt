package com.diceapp.todo.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TodoTest {

    @Test
    fun `新しいTodoを作成すると正しいデフォルト値が設定される`() {
        val todo = Todo(title = "テストタスク")
        
        assertEquals("テストタスク", todo.title)
        assertEquals("", todo.description)
        assertFalse(todo.isCompleted)
        assertEquals(Priority.MEDIUM, todo.priority)
        assertEquals("デフォルト", todo.category)
        assertTrue(todo.id.isNotEmpty())
        assertTrue(todo.createdAt.isNotEmpty())
        assertTrue(todo.updatedAt.isNotEmpty())
    }

    @Test
    fun `Todoを作成時に全てのパラメータを指定できる`() {
        val todo = Todo(
            title = "重要なタスク",
            description = "詳細な説明",
            isCompleted = true,
            priority = Priority.HIGH,
            category = "仕事",
            dueDate = "2024-12-31"
        )
        
        assertEquals("重要なタスク", todo.title)
        assertEquals("詳細な説明", todo.description)
        assertTrue(todo.isCompleted)
        assertEquals(Priority.HIGH, todo.priority)
        assertEquals("仕事", todo.category)
        assertEquals("2024-12-31", todo.dueDate)
    }

    @Test
    fun `異なるTodoは異なるIDを持つ`() {
        val todo1 = Todo(title = "タスク1")
        val todo2 = Todo(title = "タスク2")
        
        assertNotEquals(todo1.id, todo2.id)
    }

    @Test
    fun `Todoをコピーして更新できる`() {
        val originalTodo = Todo(title = "元のタスク")
        val updatedTodo = originalTodo.copy(
            title = "更新されたタスク",
            isCompleted = true
        )
        
        assertEquals("元のタスク", originalTodo.title)
        assertFalse(originalTodo.isCompleted)
        
        assertEquals("更新されたタスク", updatedTodo.title)
        assertTrue(updatedTodo.isCompleted)
        assertEquals(originalTodo.id, updatedTodo.id)
    }

    @Test
    fun `Priorityの順序が正しい`() {
        assertTrue(Priority.LOW.ordinal < Priority.MEDIUM.ordinal)
        assertTrue(Priority.MEDIUM.ordinal < Priority.HIGH.ordinal)
    }
}