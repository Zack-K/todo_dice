package com.diceapp.randomselector.model

import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SelectionTest {

    @Test
    fun `正常な選択肢でSelectionを作成できる`() {
        val items = listOf(
            SelectionItem(id = "1", text = "選択肢1"),
            SelectionItem(id = "2", text = "選択肢2")
        )
        val selection = Selection(items = items)
        
        assertEquals(2, selection.items.size)
        assertEquals("ランダム選択", selection.title)
        assertTrue(selection.id.isNotEmpty())
    }

    @Test
    fun `空の選択肢リストでエラーになる`() {
        assertFailsWith<IllegalArgumentException> {
            Selection(items = emptyList())
        }
    }

    @Test
    fun `6つを超える選択肢でエラーになる`() {
        val items = (1..7).map { 
            SelectionItem(id = it.toString(), text = "選択肢$it") 
        }
        
        assertFailsWith<IllegalArgumentException> {
            Selection(items = items)
        }
    }

    @Test
    fun `最大6つまでの選択肢は作成できる`() {
        val items = (1..6).map { 
            SelectionItem(id = it.toString(), text = "選択肢$it") 
        }
        val selection = Selection(items = items)
        
        assertEquals(6, selection.items.size)
    }
}

class SelectionItemTest {

    @Test
    fun `正常なSelectionItemを作成できる`() {
        val item = SelectionItem(id = "1", text = "テスト選択肢")
        
        assertEquals("1", item.id)
        assertEquals("テスト選択肢", item.text)
        assertEquals(1, item.weight)
    }

    @Test
    fun `重み付きのSelectionItemを作成できる`() {
        val item = SelectionItem(id = "1", text = "重要な選択肢", weight = 5)
        
        assertEquals(5, item.weight)
    }

    @Test
    fun `空白のテキストでエラーになる`() {
        assertFailsWith<IllegalArgumentException> {
            SelectionItem(id = "1", text = "")
        }
        
        assertFailsWith<IllegalArgumentException> {
            SelectionItem(id = "1", text = "   ")
        }
    }

    @Test
    fun `0以下の重みでエラーになる`() {
        assertFailsWith<IllegalArgumentException> {
            SelectionItem(id = "1", text = "テスト", weight = 0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            SelectionItem(id = "1", text = "テスト", weight = -1)
        }
    }
}

class SelectionResultTest {

    @Test
    fun `正常なSelectionResultを作成できる`() {
        val items = listOf(
            SelectionItem(id = "1", text = "選択肢1"),
            SelectionItem(id = "2", text = "選択肢2")
        )
        val selection = Selection(items = items)
        val selectedItem = items[0]
        
        val result = SelectionResult(
            selection = selection,
            diceRoll = 1,
            selectedItem = selectedItem
        )
        
        assertEquals(selection, result.selection)
        assertEquals(1, result.diceRoll)
        assertEquals(selectedItem, result.selectedItem)
        assertTrue(result.timestamp.isNotEmpty())
    }
}

class SelectionExtensionTest {

    @Test
    fun `TodoをSelectionItemに変換できる`() {
        val todo = Todo(
            title = "重要なタスク",
            description = "詳細な説明",
            priority = Priority.HIGH
        )
        
        val item = todo.toSelectionItem()
        
        assertEquals(todo.id, item.id)
        assertEquals(todo.title, item.text)
        assertEquals(1, item.weight)
    }

    @Test
    fun `TodoリストをSelectionに変換できる`() {
        val todos = listOf(
            Todo(title = "タスク1"),
            Todo(title = "タスク2"),
            Todo(title = "タスク3")
        )
        
        val selection = todos.toSelection("TODOリスト")
        
        assertEquals(3, selection.items.size)
        assertEquals("TODOリスト", selection.title)
        assertEquals("タスク1", selection.items[0].text)
        assertEquals("タスク2", selection.items[1].text)
        assertEquals("タスク3", selection.items[2].text)
    }

    @Test
    fun `空のTODOリストをSelectionに変換するとエラーになる`() {
        val emptyTodos = emptyList<Todo>()
        
        assertFailsWith<IllegalArgumentException> {
            emptyTodos.toSelection()
        }
    }

    @Test
    fun `6つを超えるTODOリストをSelectionに変換するとエラーになる`() {
        val tooManyTodos = (1..7).map { Todo(title = "タスク$it") }
        
        assertFailsWith<IllegalArgumentException> {
            tooManyTodos.toSelection()
        }
    }

    @Test
    fun `ちょうど6つのTODOリストをSelectionに変換できる`() {
        val todos = (1..6).map { Todo(title = "タスク$it") }
        val selection = todos.toSelection()
        
        assertEquals(6, selection.items.size)
    }
}