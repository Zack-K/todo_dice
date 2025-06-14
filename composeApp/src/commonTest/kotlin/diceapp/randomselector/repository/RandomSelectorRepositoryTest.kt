package com.diceapp.randomselector.repository

import com.diceapp.randomselector.model.Selection
import com.diceapp.randomselector.model.SelectionItem
import com.diceapp.randomselector.model.SelectionResult
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

class RandomSelectorRepositoryTest {

    private lateinit var repository: RandomSelectorRepository

    @Before
    fun setup() {
        repository = MockRandomSelectorRepository()
    }

    @Test
    fun `Selectionを保存できる`() = runBlocking {
        val items = listOf(
            SelectionItem(id = "1", text = "選択肢1"),
            SelectionItem(id = "2", text = "選択肢2")
        )
        val selection = Selection(items = items, title = "テスト選択")
        
        val result = repository.saveSelection(selection)
        assertTrue(result.isSuccess)
        
        val savedSelection = repository.getSelectionById(selection.id)
        assertNotNull(savedSelection)
        assertEquals(selection.title, savedSelection.title)
        assertEquals(selection.items.size, savedSelection.items.size)
    }

    @Test
    fun `SelectionResultを保存できる`() = runBlocking {
        val items = listOf(
            SelectionItem(id = "1", text = "選択肢1"),
            SelectionItem(id = "2", text = "選択肢2")
        )
        val selection = Selection(items = items)
        val result = SelectionResult(
            selection = selection,
            diceRoll = 1,
            selectedItem = items[0]
        )
        
        val saveResult = repository.saveSelectionResult(result)
        assertTrue(saveResult.isSuccess)
        
        val allResults = repository.getAllSelectionResults().first()
        assertEquals(1, allResults.size)
        assertEquals(result.diceRoll, allResults[0].diceRoll)
    }

    @Test
    fun `全てのSelectionを取得できる`() = runBlocking {
        val selection1 = Selection(items = listOf(SelectionItem("1", "選択肢1")))
        val selection2 = Selection(items = listOf(SelectionItem("2", "選択肢2")))
        
        repository.saveSelection(selection1)
        repository.saveSelection(selection2)
        
        val allSelections = repository.getAllSelections().first()
        assertEquals(2, allSelections.size)
    }

    @Test
    fun `IDでSelectionを取得できる`() = runBlocking {
        val selection = Selection(items = listOf(SelectionItem("1", "選択肢1")))
        repository.saveSelection(selection)
        
        val retrievedSelection = repository.getSelectionById(selection.id)
        assertNotNull(retrievedSelection)
        assertEquals(selection.id, retrievedSelection.id)
        
        val nonExistentSelection = repository.getSelectionById("存在しないID")
        assertNull(nonExistentSelection)
    }

    @Test
    fun `最近のSelectionResultを制限数で取得できる`() = runBlocking {
        val selection = Selection(items = listOf(SelectionItem("1", "選択肢1")))
        
        repeat(15) { i ->
            val result = SelectionResult(
                selection = selection,
                diceRoll = (i % 6) + 1,
                selectedItem = selection.items[0]
            )
            repository.saveSelectionResult(result)
        }
        
        val recentResults = repository.getRecentSelectionResults(10).first()
        assertEquals(10, recentResults.size)
    }

    @Test
    fun `Selectionを削除できる`() = runBlocking {
        val selection = Selection(items = listOf(SelectionItem("1", "選択肢1")))
        repository.saveSelection(selection)
        
        val deleteResult = repository.deleteSelection(selection.id)
        assertTrue(deleteResult.isSuccess)
        
        val retrievedSelection = repository.getSelectionById(selection.id)
        assertNull(retrievedSelection)
    }

    @Test
    fun `選択履歴をクリアできる`() = runBlocking {
        val selection = Selection(items = listOf(SelectionItem("1", "選択肢1")))
        val result = SelectionResult(
            selection = selection,
            diceRoll = 1,
            selectedItem = selection.items[0]
        )
        repository.saveSelectionResult(result)
        
        val beforeClear = repository.getAllSelectionResults().first()
        assertEquals(1, beforeClear.size)
        
        val clearResult = repository.clearSelectionHistory()
        assertTrue(clearResult.isSuccess)
        
        val afterClear = repository.getAllSelectionResults().first()
        assertEquals(0, afterClear.size)
    }

    @Test
    fun `選択統計を取得できる`() = runBlocking {
        val selection = Selection(items = listOf(
            SelectionItem("1", "選択肢1"),
            SelectionItem("2", "選択肢2")
        ))
        
        // 同じ選択肢を複数回選択
        repeat(3) {
            val result = SelectionResult(
                selection = selection,
                diceRoll = 1,
                selectedItem = selection.items[0]
            )
            repository.saveSelectionResult(result)
        }
        
        repeat(2) {
            val result = SelectionResult(
                selection = selection,
                diceRoll = 2,
                selectedItem = selection.items[1]
            )
            repository.saveSelectionResult(result)
        }
        
        val statistics = repository.getSelectionStatistics()
        assertEquals(5, statistics.totalSelections)
        assertTrue(statistics.mostUsedItems.isNotEmpty())
        assertEquals(5, statistics.selectionHistory.size)
    }
}

// テスト用のMockRepository
class MockRandomSelectorRepository : RandomSelectorRepository {
    private val selections = mutableMapOf<String, Selection>()
    private val selectionResults = mutableListOf<SelectionResult>()

    override suspend fun saveSelection(selection: Selection): Result<Unit> {
        selections[selection.id] = selection
        return Result.success(Unit)
    }

    override suspend fun saveSelectionResult(result: SelectionResult): Result<Unit> {
        selectionResults.add(result)
        return Result.success(Unit)
    }

    override suspend fun getAllSelections(): Flow<List<Selection>> {
        return flowOf(selections.values.toList())
    }

    override suspend fun getAllSelectionResults(): Flow<List<SelectionResult>> {
        return flowOf(selectionResults.toList())
    }

    override suspend fun getSelectionById(id: String): Selection? {
        return selections[id]
    }

    override suspend fun getRecentSelectionResults(limit: Int): Flow<List<SelectionResult>> {
        return flowOf(selectionResults.takeLast(limit))
    }

    override suspend fun deleteSelection(id: String): Result<Unit> {
        if (selections.containsKey(id)) {
            selections.remove(id)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Selection not found"))
    }

    override suspend fun clearSelectionHistory(): Result<Unit> {
        selectionResults.clear()
        return Result.success(Unit)
    }

    override suspend fun getSelectionStatistics(): SelectionStatistics {
        val itemCounts = mutableMapOf<String, Int>()
        
        selectionResults.forEach { result ->
            val itemText = result.selectedItem.text
            itemCounts[itemText] = itemCounts.getOrDefault(itemText, 0) + 1
        }
        
        return SelectionStatistics(
            totalSelections = selectionResults.size,
            mostUsedItems = itemCounts,
            selectionHistory = selectionResults.toList()
        )
    }
}