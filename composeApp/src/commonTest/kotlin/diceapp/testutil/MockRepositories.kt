package com.diceapp.testutil

import com.diceapp.todo.model.Todo
import com.diceapp.todo.repository.TodoRepository
import com.diceapp.dice.model.DiceRoll
import com.diceapp.dice.repository.DiceRepository
import com.diceapp.dice.repository.DiceStatistics
import com.diceapp.randomselector.model.Selection
import com.diceapp.randomselector.model.SelectionResult
import com.diceapp.randomselector.repository.RandomSelectorRepository
import com.diceapp.randomselector.repository.SelectionStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * テスト用のTodoRepositoryモック実装
 */
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

/**
 * テスト用のDiceRepositoryモック実装
 */
class MockDiceRepository : DiceRepository {
    private val rolls = mutableListOf<DiceRoll>()

    override suspend fun saveRoll(roll: DiceRoll): Result<Unit> {
        rolls.add(roll)
        return Result.success(Unit)
    }

    override suspend fun getAllRolls(): Flow<List<DiceRoll>> {
        return flowOf(rolls.toList())
    }

    override suspend fun getRollsByDiceType(sides: Int): Flow<List<DiceRoll>> {
        return flowOf(rolls.filter { it.dice.sides == sides })
    }

    override suspend fun getRecentRolls(limit: Int): Flow<List<DiceRoll>> {
        return flowOf(rolls.takeLast(limit))
    }

    override suspend fun clearHistory(): Result<Unit> {
        rolls.clear()
        return Result.success(Unit)
    }

    override suspend fun getRollStatistics(sides: Int): DiceStatistics {
        val targetRolls = rolls.filter { it.dice.sides == sides }
        
        if (targetRolls.isEmpty()) {
            return DiceStatistics(
                totalRolls = 0,
                averageResult = 0.0,
                minResult = 0,
                maxResult = 0,
                mostCommonResult = 0,
                resultDistribution = emptyMap()
            )
        }

        val allResults = targetRolls.flatMap { it.results }
        val distribution = allResults.groupingBy { it }.eachCount()
        val mostCommon = distribution.maxByOrNull { it.value }?.key ?: 0

        return DiceStatistics(
            totalRolls = targetRolls.size,
            averageResult = allResults.average(),
            minResult = allResults.minOrNull() ?: 0,
            maxResult = allResults.maxOrNull() ?: 0,
            mostCommonResult = mostCommon,
            resultDistribution = distribution
        )
    }
}

/**
 * テスト用のRandomSelectorRepositoryモック実装
 */
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