package com.diceapp.randomselector.usecase

import com.diceapp.randomselector.repository.RandomSelectorRepository
import com.diceapp.randomselector.usecase.RandomSelectorUseCase
import com.diceapp.todo.model.Priority
import com.diceapp.todo.model.Todo
import com.diceapp.todo.repository.TodoRepository
import com.diceapp.testutil.MockRandomSelectorRepository
import com.diceapp.testutil.MockTodoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RandomSelectorUseCaseTest {

    private lateinit var useCase: RandomSelectorUseCase
    private lateinit var randomSelectorRepository: RandomSelectorRepository
    private lateinit var todoRepository: TodoRepository

    @BeforeTest
    fun setup() {
        randomSelectorRepository = MockRandomSelectorRepository()
        todoRepository = MockTodoRepository()
        useCase = RandomSelectorUseCase(randomSelectorRepository, todoRepository)
    }

    @Test
    fun `テキストリストからSelectionを作成できる`() = runTest {
        val texts = listOf("選択肢1", "選択肢2", "選択肢3")
        val result = useCase.createSelectionFromTexts(texts, "テスト選択")
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { selection ->
            assertEquals("テスト選択", selection.title)
            assertEquals(3, selection.items.size)
            assertEquals("選択肢1", selection.items[0].text)
        }
    }

    @Test
    fun `空のテキストリストでエラーになる`() = runTest {
        val result = useCase.createSelectionFromTexts(emptyList())
        
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `6つを超えるテキストリストでエラーになる`() = runTest {
        val texts = (1..7).map { "選択肢$it" }
        val result = useCase.createSelectionFromTexts(texts)
        
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `TODOからSelectionを作成できる`() = runTest {
        val todo1 = Todo(title = "タスク1", category = "仕事")
        val todo2 = Todo(title = "タスク2", category = "仕事")
        
        todoRepository.insertTodo(todo1)
        todoRepository.insertTodo(todo2)
        
        val result = useCase.createSelectionFromTodos(
            listOf(todo1.id, todo2.id),
            "仕事のタスク選択"
        )
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { selection ->
            assertEquals("仕事のタスク選択", selection.title)
            assertEquals(2, selection.items.size)
            assertEquals("タスク1", selection.items[0].text)
        }
    }

    @Test
    fun `存在しないTODO IDでエラーになる`() = runTest {
        val result = useCase.createSelectionFromTodos(listOf("存在しないID"))
        
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `カテゴリからSelectionを作成できる`() = runTest {
        val todo1 = Todo(title = "仕事タスク1", category = "仕事", isCompleted = false)
        val todo2 = Todo(title = "仕事タスク2", category = "仕事", isCompleted = false)
        val todo3 = Todo(title = "完了済み", category = "仕事", isCompleted = true)
        
        todoRepository.insertTodo(todo1)
        todoRepository.insertTodo(todo2)
        todoRepository.insertTodo(todo3)
        
        val result = useCase.createSelectionFromCategory("仕事")
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { selection ->
            assertEquals(2, selection.items.size) // 完了済みは除外される
            assertTrue(selection.items.none { it.text == "完了済み" })
        }
    }

    @Test
    fun `未完了TODOがないカテゴリでエラーになる`() = runTest {
        val result = useCase.createSelectionFromCategory("存在しないカテゴリ")
        
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `Selectionからランダム選択を実行できる`() = runTest {
        val texts = listOf("選択肢1", "選択肢2", "選択肢3")
        val selectionResult = useCase.createSelectionFromTexts(texts)
        val selection = selectionResult.getOrNull()!!
        
        val fixedRandom = Random(42) // 固定シード
        val result = useCase.performSelection(selection.id, fixedRandom)
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { selectionResult ->
            assertTrue(selectionResult.diceRoll in 1..3)
            assertTrue(texts.contains(selectionResult.selectedItem.text))
        }
    }

    @Test
    fun `存在しないSelection IDでエラーになる`() = runTest {
        val result = useCase.performSelection("存在しないID")
        
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `重み付き選択を実行できる`() = runTest {
        val texts = listOf("選択肢1", "選択肢2")
        val selectionResult = useCase.createSelectionFromTexts(texts)
        val selection = selectionResult.getOrNull()!!
        
        val result = useCase.performWeightedSelection(selection.id)
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { selectionResult ->
            assertTrue(selectionResult.diceRoll in 1..2)
            assertTrue(texts.contains(selectionResult.selectedItem.text))
        }
    }

    @Test
    fun `全TODOからクイック選択を実行できる`() = runTest {
        // 未完了のTODOを追加
        repeat(3) { i ->
            val todo = Todo(title = "タスク${i + 1}", isCompleted = false)
            todoRepository.insertTodo(todo)
        }
        
        val result = useCase.quickSelectFromAllTodos()
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { selectionResult ->
            assertTrue(selectionResult.selectedItem.text.startsWith("タスク"))
        }
    }

    @Test
    fun `未完了TODOがない場合のクイック選択でエラーになる`() = runTest {
        // 完了済みのTODOのみ追加
        val completedTodo = Todo(title = "完了済み", isCompleted = true)
        todoRepository.insertTodo(completedTodo)
        
        val result = useCase.quickSelectFromAllTodos()
        
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `Selectionを削除できる`() = runTest {
        val texts = listOf("選択肢1")
        val selectionResult = useCase.createSelectionFromTexts(texts)
        val selection = selectionResult.getOrNull()!!
        
        val deleteResult = useCase.deleteSelection(selection.id)
        assertTrue(deleteResult.isSuccess)
        
        val performResult = useCase.performSelection(selection.id)
        assertFalse(performResult.isSuccess)
    }

    @Test
    fun `選択履歴をクリアできる`() = runTest {
        val texts = listOf("選択肢1")
        val selectionResult = useCase.createSelectionFromTexts(texts)
        val selection = selectionResult.getOrNull()!!
        
        useCase.performSelection(selection.id)
        
        val beforeClear = useCase.getAllSelectionResults().first()
        assertEquals(1, beforeClear.size)
        
        val clearResult = useCase.clearSelectionHistory()
        assertTrue(clearResult.isSuccess)
        
        val afterClear = useCase.getAllSelectionResults().first()
        assertEquals(0, afterClear.size)
    }

    @Test
    fun `利用可能なカテゴリを取得できる`() = runTest {
        val todo1 = Todo(title = "タスク1", category = "仕事")
        val todo2 = Todo(title = "タスク2", category = "プライベート")
        
        todoRepository.insertTodo(todo1)
        todoRepository.insertTodo(todo2)
        
        val categories = useCase.getAvailableCategories()
        assertTrue(categories.contains("仕事"))
        assertTrue(categories.contains("プライベート"))
    }

    @Test
    fun `選択統計を取得できる`() = runTest {
        val texts = listOf("選択肢1", "選択肢2")
        val selectionResult = useCase.createSelectionFromTexts(texts)
        val selection = selectionResult.getOrNull()!!
        
        useCase.performSelection(selection.id)
        useCase.performSelection(selection.id)
        
        val statistics = useCase.getSelectionStatistics()
        assertEquals(2, statistics.totalSelections)
        assertTrue(statistics.mostUsedItems.isNotEmpty())
    }
}